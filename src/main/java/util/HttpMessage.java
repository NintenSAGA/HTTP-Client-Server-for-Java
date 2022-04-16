package util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
@Getter
public abstract class HttpMessage {
    public static final String HTTP10 = "HTTP/1";
    public static final String HTTP11 = "HTTP/1.1";
    public static final String HTTP20 = "HTTP/2";

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

    @NonNull private final String httpVersion;
    @NonNull private final Map<String, String> headers;
    @NonNull private String body;
    private byte[] binaryBody;

    public HttpMessage() {
        httpVersion = HTTP11;
        headers = new HashMap<>();
        body = "";
        binaryBody = null;
    }

    public boolean isBodyBinary() {
        return binaryBody != null;
    }

    public void addHeader(String key, String val) { headers.put(key, val); }

    public void setBodyAsFile(String path) {
        String[] a = path.split("\\.");
        String suffix = a[a.length - 1];
        MediaType mediaType = suffixToMime.getOrDefault(suffix, suffixToMime.get("default"));
        Log.debug("File %s sent as %s".formatted(path, mediaType));
        if (MediaType.BINARY_TYPE.contains(mediaType)) {
            binaryBody = Config.getResourceAsByteArray(path);
            headers.put("Content-Type", "%s".formatted(mediaType));
            headers.put("Content-Length", "%d".formatted(binaryBody.length));
            return;
        }

        headers.put("Content-Type", "%s; charset=UTF-8".formatted(mediaType));
        String content = Config.getResourceAsString(path);
        setBodyWithChunked(content);
    }

    /**
     * Set body as plain text and calculate content-length automatically
     */
    public void setBodyAsPlainText(String body) {
        headers.put("Content-Type", "text/plain; charset=UTF-8");
        if (body.length() <= CHUNK_SIZE)
            setBodyWithContentLength(body);
        else
            setBodyWithChunked(body);
    }

    public void setBodyAsHTML(String body) {
        headers.put("Content-Type", "text/html; charset=UTF-8");
        setBodyWithChunked(body);
    }

    protected void setBodyWithContentLength(String body) {
        headers.put("Content-Length", String.valueOf(body.getBytes().length));
        this.body = body;
    }

    protected void setBodyWithChunked(String body) {
        headers.put("Transfer-Encoding", "chunked");
        StringBuilder sb = new StringBuilder();
        int st = 0;

        for (; st < body.length(); st += CHUNK_SIZE) {
            String chunk = body.substring(st, Math.min(st + CHUNK_SIZE, body.length()));
            sb.append("%s\r\n%s\r\n".formatted(
                    Integer.toString(chunk.getBytes().length,16),
                    chunk
            ));
        }

        sb.append("0\r\n\r\n");
        this.body = sb.toString();
    }

    protected String flatMessage(String startLine) {
        StringBuilder sb = new StringBuilder();
        sb.append(startLine);      sb.append("\r\n");
        headers.forEach((k, v) -> sb.append("%s: %s\r\n".formatted(k, v)));
        sb.append("\r\n");
        sb.append(body);
        return sb.toString();
    }

    protected byte[] flatMessageToBinary(String startLine) {
        StringBuilder sb = new StringBuilder();
        sb.append(startLine);      sb.append("\r\n");
        headers.forEach((k, v) -> sb.append("%s: %s\r\n".formatted(k, v)));
        sb.append("\r\n");
        byte[] a = sb.toString().getBytes(StandardCharsets.UTF_8);
        byte[] ret = Arrays.copyOf(a, a.length + binaryBody.length);
        System.arraycopy(binaryBody, 0, ret, a.length, binaryBody.length);

        return ret;
    }
}
