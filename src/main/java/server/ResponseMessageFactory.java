package server;

import util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * A singleton factory <br/>
 * Supporting 200, 301, 302, 304, 404, 405, 500
 */
public class ResponseMessageFactory {
    private static final ResponseMessageFactory instance = new ResponseMessageFactory();
    public static ResponseMessageFactory getInstance(){ return instance; }
    private ResponseMessageFactory() {}

    /**
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/200>200 OK</a><br/>
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/301>301 Moved Permanently</a> need arg: location<br/>
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/302>302 Found</a> need arg: location<br/>
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/304>304 Not Modified</a><br/>
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/404>404 Not Found</a><br/>
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/405>405 Method Not Allowed</a><br/>
     */
    public HttpResponseMessage produce(int code, String ... args) {
        try {
            Log.debug("produce" + code);
            Method m = this.getClass().getDeclaredMethod("produce" + code, String[].class);
            try {
                return (HttpResponseMessage) m.invoke(this, (Object) args);
            } catch (InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
                return produce500();
            }
        } catch (NoSuchMethodException e) {
            return new HttpResponseMessage(code);
        }
    }

    /**
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/301>301 Moved Permanently</a>
     */
    private HttpResponseMessage produce301(String[] args) {
        if (args.length < 1) return produce500();
        HttpResponseMessage hrm = new HttpResponseMessage(301);
        hrm.setBody(args[0]);
        return hrm;
    }

    /**
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/302>302 Found</a>
     */
    private HttpResponseMessage produce302(String[] args) {
        if (args.length < 1) return produce500();
        HttpResponseMessage hrm = new HttpResponseMessage(301);
        hrm.setBody(args[0]);
        return hrm;
    }

    private HttpResponseMessage produce500() {
        return produce500(null);
    }

    /**
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/500>500 Internal Server Error</a>
     */
    private HttpResponseMessage produce500(String[] args) {
        return new HttpResponseMessage(500);
    }
}
