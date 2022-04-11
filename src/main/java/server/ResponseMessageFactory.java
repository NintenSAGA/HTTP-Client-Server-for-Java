package server;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * A singleton factory <br/>
 * Supporting 200, 301, 302, 304, 404, 405, 500
 */
public class ResponseMessageFactory {
    private static ResponseMessageFactory instance = new ResponseMessageFactory();
    public static ResponseMessageFactory getInstance(){ return instance; }
    private ResponseMessageFactory() {}

    /**
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/200>200 OK</a>
     */
    public HttpResponseMessage produce200() {
        // todo: 200 OK
        return new HttpResponseMessage(200);
    }

    /**
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/301>301 Moved Permanently</a>
     */
    public HttpResponseMessage produce301(String location) {
        // todo: 301 Moved Permanently
        return null;
    }

    /**
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/302>302 Found</a>
     */
    public HttpResponseMessage produce302(String location) {
        // todo: 302 Found
        return null;
    }

    /**
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/304>304 Not Modified</a>
     */
    public HttpResponseMessage produce304() {
        // todo: 304 Not Modified
        return null;
    }

    /**
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/404>404 Not Found</a>
     */
    public HttpResponseMessage produce404() {
        // todo: 404 Not Found
        return null;
    }

    /**
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/405>405 Method Not Allowed</a>
     */
    public HttpResponseMessage produce405() {
        // todo: 405 Method Not Allowed
        return null;
    }

    /**
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/500>500 Internal Server Error</a>
     */
    public HttpResponseMessage produce500() {
        // todo: 500 Internal Server Error
        return null;
    }
}
