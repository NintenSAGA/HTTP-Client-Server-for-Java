package util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

@Getter
public abstract class HttpMessage {
    public static final String HTTP10       = "HTTP/1";
    public static final String HTTP11       = "HTTP/1.1";
    public static final String HTTP20       = "HTTP/2";
    public static final int ZIP_THRESHOLD   = (1 << 10);  // 1 KB

    private static final int CHUNK_SIZE = 500;     // chunk size in char

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

    @NonNull private final  String httpVersion;
    @NonNull private final  Map<String, String> headers;
    @NonNull private byte[] body;

    public HttpMessage() {
        httpVersion = HTTP11;
        headers     = new HashMap<>();
        body        = new byte[0];
    }

    public HttpMessage(String httpVersion, Map<String, String> headers, String body) {
        this.httpVersion = httpVersion;
        this.headers = headers;
        this.setBodyAsPlainText(body);
    }

    public String getBody() {
        return new String(body);
    }

    public void addHeader(String key, String val) { headers.put(key, val); }

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
        body = Config.getResourceAsByteArray(path);

        if (body.length > ZIP_THRESHOLD) {
            Log.debug("Body was zipped with GZIP");
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                GZIPOutputStream gzipOut = new GZIPOutputStream(bos, true);
                gzipOut.write(body, 0, body.length);
                gzipOut.finish();
                body = bos.toByteArray();
                headers.put("Content-Encoding", "gzip");
            } catch (IOException e) {
                e.printStackTrace();
                Log.debug("Zipping failed!");
            }
            setBodyWithChunked(body);
//            setBodyWithContentLength(body);
        } else {
            setBodyWithContentLength(body);
        }
    }

    /**
     * Set body as plain text and calculate content-length automatically
     */
    public void setBodyAsPlainText(String body) {
        headers.put("Content-Type", "text/plain; charset=UTF-8");
        setBodyWithContentLength(body.getBytes(StandardCharsets.UTF_8));
    }

    protected void setBodyWithContentLength(byte[] a) {
        Log.debug("Content-Length: ", a.length >>> 10, "KB" );
        headers.put("Content-Length", String.valueOf(a.length));
        this.body = a;
    }

    protected void setBodyWithChunked(byte[] a) {
        headers.put("Transfer-Encoding", "chunked");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            for (int st = 0; st < a.length; st += CHUNK_SIZE) {
                int len = Math.min(CHUNK_SIZE, a.length - st);
                bos.write("%s\r\n".formatted(Integer.toString(len, 16)).getBytes());
                bos.write(a, st, len);
                bos.write("\r\n".getBytes());
            }
            bos.write("0\r\n\r\n".getBytes());
            this.body = bos.toByteArray();
            assert body.length > a.length;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected String flatMessage(String startLine) {
        StringBuilder sb = new StringBuilder();
        sb.append(startLine);      sb.append("\r\n");
        headers.forEach((k, v) -> sb.append("%s: %s\r\n".formatted(k, v)));
        sb.append("\r\n");
        sb.append(new String(body, StandardCharsets.UTF_8));
        return sb.toString();
    }

    protected byte[] flatMessageToBinary(String startLine) {
        StringBuilder sb = new StringBuilder();
        sb.append(startLine);      sb.append("\r\n");
        headers.forEach((k, v) -> sb.append("%s: %s\r\n".formatted(k, v)));
        sb.append("\r\n");
        byte[] a = sb.toString().getBytes(StandardCharsets.UTF_8);
        byte[] ret = Arrays.copyOf(a, a.length + body.length);
        System.arraycopy(body, 0, ret, a.length, body.length);

        return ret;
    }
}
