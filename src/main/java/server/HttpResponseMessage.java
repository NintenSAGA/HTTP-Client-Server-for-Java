package server;

import lombok.Getter;
import lombok.Setter;
import util.HttpMessage;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Getter
public class HttpResponseMessage extends HttpMessage {
    private static final Map<Integer, String> defaultStatusTextMap;
    static {
        defaultStatusTextMap = new HashMap<>();
        String raw = """
                200,OK               \s
                301,Moved Permanently\s
                302,Found            \s
                304,Not Modified     \s
                404,Not Found        \s
                405,Method Not Allowed""";
        Arrays.stream(raw.split("\n")).forEach(s -> {
            String[] a = s.split(",");
            defaultStatusTextMap.put(Integer.parseInt(a[0]), a[1]);
        });
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
