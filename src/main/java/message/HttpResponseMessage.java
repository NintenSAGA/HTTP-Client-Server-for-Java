package message;

import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;
import util.Config;

import java.util.HashMap;
import java.util.Map;

@Getter
public class HttpResponseMessage extends HttpMessage {
    private static final Map<Integer, String> defaultStatusTextMap;
    static {
        defaultStatusTextMap = new HashMap<>();
        JSONObject jsonObject = Config.getConfigAsJsonObj(Config.DEFAULT_STATUS_TEXT);
        jsonObject.keySet().forEach(k -> defaultStatusTextMap.put(Integer.parseInt(k), jsonObject.getString(k)));
    }
    private final int statusCode;
    @Setter private String statusText;

    public HttpResponseMessage(int statusCode, String statusText) {
        super();
        this.statusCode = statusCode;
        this.statusText = statusText;
    }

    public HttpResponseMessage(String httpVersion, int statusCode, String statusText, Map<String, String> headers, byte[] body) {
        super(httpVersion, headers, body);
        this.statusCode = statusCode;
        this.statusText = statusText;
    }

    public HttpResponseMessage(String httpVersion, String statusCode, String statusText, Map<String, String> headers, byte[] body)
    throws NumberFormatException
    {
        this(httpVersion, Integer.parseInt(statusCode), statusText, headers, body);
    }

    public HttpResponseMessage(String httpVersion, int statusCode, String statusText, Map<String, String> headers, String body) {
        this(httpVersion, statusCode, statusText, headers, body.getBytes());
    }

    public HttpResponseMessage(int statusCode) {
        this(statusCode, defaultStatusText(statusCode));
    }

    @Override
    protected String getStartLine() {
        return "%s %s %s".formatted(getHttpVersion(), getStatusCode(), getStatusText());
    }

    public void addCookie(String key, String val) {
        String expr = "%s=%s".formatted(key, val);
        mergeHeader("Set-Cookie", expr, "%s; %s"::formatted);
    }

    /**
     * Returning the default status text of the corresponding status code <br/>
     * Referring to <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status>mdn web docs<a/>
     * @param statusCode status code
     * @return default status text
     */
    static String defaultStatusText(int statusCode) {
        return defaultStatusTextMap.get(statusCode);
    }


}
