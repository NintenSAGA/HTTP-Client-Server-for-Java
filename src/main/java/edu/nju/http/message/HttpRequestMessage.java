package edu.nju.http.message;

import lombok.Getter;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Getter
public class HttpRequestMessage extends HttpMessage {
    private final String method;
    private final String target;

    public HttpRequestMessage(String method, String target) {
        super();
        this.method = method;
        this.target = target;
    }

    public HttpRequestMessage(String method, String target, String httpVersion, Map<String, String> headers, byte[] body) {
        super(httpVersion, headers, body);
        this.method = method;
        this.target = target;
    }

    public HttpRequestMessage(String method, String target, String httpVersion, Map<String, String> headers, String body) {
        this(method, target, httpVersion, headers, body.getBytes());
    }

    @Override
    protected String getStartLine() {
        return "%s %s %s".formatted(getMethod(), getTarget(), getHttpVersion());
    }

    public Map<String, String> getCookies() {
        if (!containsHeader("Cookie".toLowerCase(Locale.ROOT))) return null;
        Map<String, String> cookies = new HashMap<>();
        Arrays.stream(getHeaderVal("Cookie".toLowerCase(Locale.ROOT)).split("; "))
                .map(expr -> expr.split("="))
                .forEach(a -> cookies.put(a[0], a[1]));
        return cookies;
    }

    public void setBodyAsFormUrlencoded(String body) {
        this.setBodyWithType(
                URLEncoder.encode(body, StandardCharsets.UTF_8).getBytes(StandardCharsets.UTF_8),
                "application/x-www-form-urlencoded");
    }
}
