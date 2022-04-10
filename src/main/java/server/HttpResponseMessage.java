package server;

import lombok.Getter;
import util.HttpMessage;

@Getter
public class HttpResponseMessage extends HttpMessage {
    private final int statusCode;
    private final String statusText;

    public HttpResponseMessage(int statusCode) {
        super();
        this.statusCode = statusCode;
        this.statusText = defaultStatusText(statusCode);
    }

    public String flatMessage() {
        String startLine = "%s %s %s".formatted(getHttpVersion(), getStatusCode(), getStatusText());
        return flatMessage(startLine);
    }

    /**
     * Returning the default status text of the corresponding status code <br/>
     * Referring to <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status>mdn web docs<a/>
     * @param statusCode status code
     * @return default status text
     */
    static String defaultStatusText(int statusCode) {
        // todo: defaultStatusText
        return "Not implemented yet";
    }
}
