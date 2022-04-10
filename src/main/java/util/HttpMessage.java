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
    @NonNull @Setter private String body;

    public HttpMessage() {
        httpVersion = HTTP11;
        headers = new HashMap<>();
        body = "";
    }

    public void addHeader(String key, String val) { headers.put(key, val); }

    protected String flatMessage(String startLine) {
        StringBuilder sb = new StringBuilder();
        sb.append(sb);      sb.append("\r\n");
        headers.forEach((k, v) -> sb.append("%s: %s\r\n"));
        sb.append("\r\n");
        sb.append(body);
        return sb.toString();
    }
}
