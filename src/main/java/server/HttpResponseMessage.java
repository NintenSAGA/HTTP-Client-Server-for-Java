package server;

import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;
import util.Config;
import util.HttpMessage;

import java.util.Arrays;
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

    public HttpResponseMessage(int statusCode) {
        this(statusCode, defaultStatusText(statusCode));
    }

    public String flatMessage() {
        String startLine = "%s %s %s".formatted(getHttpVersion(), getStatusCode(), getStatusText());
        return flatMessage(startLine);
    }

    public byte[] flatMessageToBinary() {
        String startLine = "%s %s %s".formatted(getHttpVersion(), getStatusCode(), getStatusText());
        return flatMessageToBinary(startLine);
    }

    public void addCookie(String key, String val) {
        String expr = "%s=%s".formatted(key, val);
        getHeaders().merge("Set-Cookie", expr, "%s; %s"::formatted);
    }

    @Override
    public String toString() {
        return this.flatMessage();
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
