package util;

import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.zip.GZIPOutputStream;

import static util.consts.TransferEncoding.*;

public abstract class HttpMessage {
    public static final String HTTP10       = "HTTP/1";
    public static final String HTTP11       = "HTTP/1.1";
    public static final String HTTP20       = "HTTP/2";
    public static final int ZIP_THRESHOLD   = (1 << 10);  // 1 KB
    public static final long LARGE_FILE   = 100 * (1 << 20);  // 100 MB

    private static final int CHUNK_SIZE = 500;     // chunk size in char

    private static final Map<String, MediaType> suffixToMime;
    static {
        suffixToMime = new HashMap<>();
        JSONObject json = Config.getConfigAsJsonObj(Config.MIME);
        for (String codeType : json.keySet()) {
            JSONObject codeTypeJson = json.getJSONObject(codeType);
            for (String type : codeTypeJson.keySet()) {
                JSONObject temp = codeTypeJson.getJSONObject(type);
                temp.keySet().forEach(suffix -> {
                    MediaType mediaType = new MediaType(type, temp.getString(suffix));
                    suffixToMime.put(suffix, mediaType);
                    if (codeType.equals("binary"))
                        MediaType.BINARY_TYPE.add(mediaType);
                });
            }
        }
    }

    // ====================== Nested Class ========================= //

    @Data
    private static class MediaType {
        private final static Set<MediaType> BINARY_TYPE;
        static {
            BINARY_TYPE = new HashSet<>();
        }
        @NonNull String type;
        @NonNull String subtype;
        @Override
        public String toString() {
            return "%s/%s".formatted(type, subtype);
        }
    }

    @NonNull @Getter private final  String httpVersion;
    @NonNull @Getter private final  Map<String, String> headers;

    @NonNull private byte[] body;

    private InputStream bodyStream;

    // ====================== Public ========================= //

    public HttpMessage() {
        httpVersion = HTTP11;
        headers     = new HashMap<>();
        body        = new byte[0];
    }

    public HttpMessage(String httpVersion, Map<String, String> headers, byte[] body) {
        this.httpVersion = httpVersion;
        this.headers = headers;
        this.body = Objects.requireNonNullElseGet(body, () -> new byte[0]);
    }

    public HttpMessage(String httpVersion, Map<String, String> headers, String body) {
        this(httpVersion, headers, body.getBytes());
    }

    /**
     * Containing the trailing CRLF
     */
    public String getStartLineAndHeaders() {
        return
                getStartLine()  + "\r\n" +
                getHeadersAsString()    + "\r\n";
    }

    public InputStream getStartLineAndHeadersAsStream() {
        return new ByteArrayInputStream(
                getStartLineAndHeaders().getBytes());
    }

    public InputStream getBodyAsStream() {
        if (bodyStream != null) return bodyStream;
        return new ByteArrayInputStream(body);
    }

    public byte[] getBodyAsBytes() {
        return body;
    }

    public String getBodyAsString() {
        return new String(getBodyAsBytes());
    }

    public String flatMessage() {
        return new String(flatMessageToBinary(), StandardCharsets.UTF_8);
    }

    public byte[] flatMessageToBinary() {
        byte[] a = getStartLineAndHeaders().getBytes(StandardCharsets.UTF_8);
        byte[] body;

        try {
            body = getBodyAsStream().readAllBytes();
            byte[] ret = Arrays.copyOf(a, a.length + body.length);
            System.arraycopy(body, 0, ret, a.length, body.length);
            return ret;
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }


    // -------------------- Header Setting -------------------- //

    public void addHeader(String key, String val) { headers.put(key, val); }

    public void putAllHeaders(Map<String, String> headers) { this.headers.putAll(headers); }

    public String getHeaderVal(String key) { return headers.get(key.toLowerCase(Locale.ROOT)); }

    public boolean containsHeader(String key) { return headers.containsKey(key.toLowerCase(Locale.ROOT)); }

    public void mergeHeader(String a, String b, BiFunction<String, String, String> func) { headers.merge(a, b, func); }


    // -------------------- Body Setting -------------------- //

    public void setBodyAsFile(String path) {
        String[] a = path.split("\\.");
        String suffix = a[a.length - 1];
        MediaType mediaType = suffixToMime.getOrDefault(suffix, suffixToMime.get("default"));
        Log.debug("File %s sent as %s".formatted(path, mediaType));
        if (MediaType.BINARY_TYPE.contains(mediaType)) {
            Log.debug(mediaType, " is binary");
            headers.put("Content-Type", "%s".formatted(mediaType));
        } else {
            headers.put("Content-Type", "%s; charset=UTF-8".formatted(mediaType));
        }

        long fileSize = Config.getSizeOfResource(path);

        if (fileSize >= LARGE_FILE) {
            Log.logServer("File[%s] size: %.2fMB".formatted(path, (double) fileSize / (1 << 20)));
            bodyStream = Config.getResourceAsStream(path);
        } else {
            if (fileSize >= (1 << 20))
                Log.logServer("File[%s] size: %.2fMB".formatted(path, (double) fileSize / (1 << 20)));
            else
                Log.logServer("File[%s] size: %.2fKB".formatted(path, (double) fileSize / (1 << 10)));
            byte[] bytes = Config.getResourceAsByteArray(path);
            bodyStream = null;
            setBody(bytes);

            if (body.length > ZIP_THRESHOLD) {
                bodyGzipEncode();
            }
        }
    }

    /**
     * Set body as plain text and calculate content-length automatically
     */
    public void setBodyAsPlainText(String body) {
        headers.put("Content-Type", "text/plain; charset=UTF-8");
        setBody(body.getBytes());
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    /**
     * Force setting body as plain and set transfer-encoding as chunked
     */
    @Deprecated
    public void setBodyAsPlainTextChunked(String body) {
        headers.put("Content-Type", "text/plain; charset=UTF-8");
        setBodyWithChunked(body.getBytes(StandardCharsets.UTF_8));
    }

    // ====================== Protected ========================= //

    @Deprecated
    protected void setBodyWithContentLength(byte[] a) {
        Log.debug("Content-Length: ", a.length >>> 10, "KB" );
        headers.put(CONTENT_LENGTH, String.valueOf(a.length));
        setBody(a);
    }

    @Deprecated
    protected void setBodyWithChunked(byte[] a) {
        headers.put("Transfer-Encoding", CHUNKED);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            for (int st = 0; st < a.length; st += CHUNK_SIZE) {
                int len = Math.min(CHUNK_SIZE, a.length - st);
                bos.write("%s\r\n".formatted(Integer.toString(len, 16)).getBytes());
                bos.write(a, st, len);
                bos.write("\r\n".getBytes());
            }
            bos.write("0\r\n\r\n".getBytes());
            setBody(bos.toByteArray());
            assert body.length > a.length;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected abstract String getStartLine();

    // ==================== Private ==================== //

    private String getHeadersAsString() {
        StringBuilder sb = new StringBuilder();
        headers.forEach((k, v) -> sb.append("%s: %s\r\n".formatted(k, v)));
        return sb.toString();
    }

    private void bodyGzipEncode() {
        Log.debug("Body was zipped with GZIP");
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            GZIPOutputStream gzipOut = new GZIPOutputStream(bos, true);
            gzipOut.write(getBodyAsBytes(), 0, body.length);
            gzipOut.finish();
            setBody(bos.toByteArray());
            headers.put("Content-Encoding", GZIP);
        } catch (IOException e) {
            e.printStackTrace();
            Log.debug("Zipping failed!");
        }
    }
}
