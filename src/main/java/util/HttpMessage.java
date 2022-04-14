package util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@Getter
public abstract class HttpMessage {
    public static final String HTTP10 = "HTTP/1";
    public static final String HTTP11 = "HTTP/1.1";
    public static final String HTTP20 = "HTTP/2";

    private static final int CHUNK_SIZE = 500;     // chunk size in char

    @Data
    private static class MediaType {
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
        for (String type : json.keySet()) {
            JSONObject temp = json.getJSONObject(type);
            temp.keySet().forEach(suffix -> suffixToMime.put(suffix, new MediaType(type, temp.getString(suffix))));
        }
    }

    @NonNull private final String httpVersion;
    @NonNull private final Map<String, String> headers;
    @NonNull private String body;

    public HttpMessage() {
        httpVersion = HTTP11;
        headers = new HashMap<>();
        body = "";
    }

    public void addHeader(String key, String val) { headers.put(key, val); }

    public void setBodyAsFile(String path) {
        String content = Config.getResourceAsString(path);
        String[] a = path.split("\\.");
        String suffix = a[a.length - 1];
        MediaType mediaType = suffixToMime.getOrDefault(suffix, suffixToMime.get("default"));
        headers.put("Content-Type", "%s; charset=UTF-8".formatted(mediaType));
        Log.debug("File %s sent as %s".formatted(path, mediaType));
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

    private void setBodyWithContentLength(String body) {
        headers.put("Content-Length", String.valueOf(body.getBytes().length));
        this.body = body;
    }

    private void setBodyWithChunked(String body) {
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
}
