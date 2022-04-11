package client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import util.HttpMessage;

import java.util.HashMap;

@Getter
public class HttpRequestMessage extends HttpMessage {
    private final String method;
    private final String target;

    public HttpRequestMessage(String method, String target) {
        super();
        this.method = method;
        this.target = target;
    }

    public HttpRequestMessage(String method, String target, String httpVersion, HashMap<String, String> headers, String body) {
        super(httpVersion, headers, body);
        this.method = method;
        this.target = target;
    }

    public String flatMessage() {
        String startLine = "%s %s %s".formatted(getMethod(), getTarget() ,getHttpVersion());
        return flatMessage(startLine);
    }

    @Override
    public String toString() {
        return this.flatMessage();
    }
}
