package util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.HashMap;

@AllArgsConstructor
@Getter
public abstract class HttpMessage {
    static final String HTTP10 = "HTTP/1";
    static final String HTTP11 = "HTTP/1.1";
    static final String HTTP20 = "HTTP/2";

    @NonNull private final String httpVersion;
    @NonNull private final HashMap<String, String> headers;
    @NonNull private String body;

    public HttpMessage() {
        httpVersion = HTTP11;
        headers = new HashMap<>();
        body = "";
    }

    public void addHeader(String key, String val) { headers.put(key, val); }

    /**
     * Set body and calculate content-length automatically
     */
    public void setBody(String body) {
        headers.put("Content-Length", String.valueOf(body.getBytes().length));
        this.body = body;
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
