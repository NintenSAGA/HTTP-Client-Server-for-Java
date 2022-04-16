package client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import util.HttpMessage;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Getter
public class HttpRequestMessage extends HttpMessage {
    private final String method;
    private final String target;

    public HttpRequestMessage(String method, String target) {
        super();
        this.method = method;
        this.target = URLEncoder.encode(target, StandardCharsets.UTF_8);
    }

    public HttpRequestMessage(String method, String target, String httpVersion, Map<String, String> headers, String body) {
        super(httpVersion, headers, body);
        this.method = method;
        this.target = URLDecoder.decode(target, StandardCharsets.UTF_8);
    }

    public String flatMessage() {
        String startLine = "%s %s %s".formatted(getMethod(), getTarget() ,getHttpVersion());
        return flatMessage(startLine);
    }

    public Map<String, String> getCookies() {
        if (!getHeaders().containsKey("Cookie")) return null;
        Map<String, String> cookies = new HashMap<>();
        Arrays.stream(getHeaders().get("Cookie").split("; "))
                .map(expr -> expr.split("="))
                .forEach(a -> cookies.put(a[0], a[1]));
        return cookies;
    }

    public void setBodyAsFormUrlencoded(String body) {
        this.getHeaders().put("Content-Type", "application/x-www-form-urlencoded");
        this.setBodyWithContentLength(URLEncoder.encode(body, StandardCharsets.UTF_8).getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String toString() {
        return this.flatMessage();
    }
}
