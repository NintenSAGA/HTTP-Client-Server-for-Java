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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;

import static util.consts.Headers.*;

public abstract class HttpMessage {
    public static final String HTTP10       = "HTTP/1";
    public static final String HTTP11       = "HTTP/1.1";
    public static final String HTTP20       = "HTTP/2";


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

    private InputStream bodyStream;

    // ====================== Public ========================= //

    public HttpMessage() {
        httpVersion = HTTP11;
        headers     = new HashMap<>();
    }

    public HttpMessage(String httpVersion, Map<String, String> headers, byte[] body) {
        this.httpVersion = httpVersion;
        this.headers = headers;

        byte[] bytes = Objects.requireNonNullElseGet(body, () -> new byte[0]);
        bodyStream = new ByteArrayInputStream(bytes);
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
        else return new ByteArrayInputStream(new byte[0]);
    }

    public byte[] getBodyAsBytes() {
        try {
            return getBodyAsStream().readAllBytes();
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    public String getBodyAsString() {
        return new String(getBodyAsBytes());
    }

    public String flatMessage() {
        return new String(flatMessageToBinary(), StandardCharsets.UTF_8);
    }

    public byte[] flatMessageToBinary() {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write(getStartLineAndHeaders().getBytes());
            out.write(getBodyAsBytes());
            return out.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    @Override
    public String toString() {
        return this.flatMessage();
    }

    // -------------------- Header Setting -------------------- //

    public void addHeader(String key, String val) { headers.put(key, val); }

    public void putAllHeaders(Map<String, String> headers) { this.headers.putAll(headers); }

    public String getHeaderVal(String key) { return headers.get(key.toLowerCase(Locale.ROOT)); }

    public boolean containsHeader(String key) { return headers.containsKey(key.toLowerCase(Locale.ROOT)); }

    public void mergeHeader(String a, String b, BiFunction<String, String, String> func) { headers.merge(a, b, func); }


    // -------------------- Body Setting -------------------- //
    public void setBody(String body) {
        setBody(body.getBytes());
    }

    public void setBody(byte[] body) {
        setBody(new ByteArrayInputStream(body), body.length);
    }

    public void setBody(InputStream stream, long length) {
        this.bodyStream = stream;
        addHeader(CONTENT_LENGTH, String.valueOf(length));
    }

    public void setBodyType(String type) {
        headers.put("Content-Type", type);
    }

    public void setBodyWithType(byte[] body, String type) {
        setBodyType(type);
        setBody(body);
    }

    public void setBodyWithType(String body, String type) {
        setBodyWithType(body.getBytes(), type);
    }

    /**
     * Set body as plain text and calculate content-length automatically
     */
    public void setBodyAsPlainText(String body) {
        setBodyWithType(body, "text/plain; charset=UTF-8");
    }

    public void setBodyAsFile(String path) {
        String[] a = path.split("\\.");
        String suffix = a[a.length - 1];
        MediaType mediaType = suffixToMime.getOrDefault(suffix, suffixToMime.get("default"));
        Log.debug("File %s sent as %s".formatted(path, mediaType));

        String type;
        if (MediaType.BINARY_TYPE.contains(mediaType)) {
            Log.debug(mediaType, " is binary");
            type = "%s".formatted(mediaType);
        } else {
            type = "%s; charset=UTF-8".formatted(mediaType);
        }

        long fileSize = Config.getSizeOfResource(path);

        if (fileSize >= (1 << 20))
            Log.logServer("File[%s] size: %.2fMB".formatted(path, (double) fileSize / (1 << 20)));
        else
            Log.logServer("File[%s] size: %.2fKB".formatted(path, (double) fileSize / (1 << 10)));

        setBodyType(type);
        setBody(Config.getResourceAsStream(path), fileSize);
    }

    // ====================== Protected ========================= //

    protected abstract String getStartLine();

    // ==================== Private ==================== //

    private String getHeadersAsString() {
        StringBuilder sb = new StringBuilder();
        headers.forEach((k, v) -> sb.append("%s: %s\r\n".formatted(k, v)));
        return sb.toString();
    }

//    private void bodyGzipEncode() {
//        Log.debug("Body was zipped with GZIP");
//        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
//            GZIPOutputStream gzipOut = new GZIPOutputStream(bos, true);
//            gzipOut.write(getBodyAsBytes(), 0, body.length);
//            gzipOut.finish();
//            setBody(bos.toByteArray());
//            headers.put("Content-Encoding", GZIP);
//        } catch (IOException e) {
//            e.printStackTrace();
//            Log.debug("Zipping failed!");
//        }
//    }
}
