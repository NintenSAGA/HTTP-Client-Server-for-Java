package edu.nju.http.message;

import edu.nju.http.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * A singleton factory <br/>
 * Supporting 200, 301, 302, 304, 404, 405, 500
 */
public class ResponseMessageFactory {
    private static final ResponseMessageFactory instance = new ResponseMessageFactory();

    public static ResponseMessageFactory getInstance() {
        return instance;
    }

    private ResponseMessageFactory() {
    }

    /**
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/100>100 Continue</a><br/>
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/101>101 Switching Protocols</a><br/>
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/103>103 Early Hints</a><br/>
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/200>200 OK</a><br/>
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/201>201 Created</a><br/>
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/202>202 Accepted</a><br/>
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/203>203 Non-Authoritative Information</a><br/>
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/204>204 No Content</a><br/>
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/205>205 Reset Content</a><br/>
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/206>206 Partial Content</a><br/>
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/300>300 Multiple Choices</a><br/>
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/301>301 Moved Permanently</a><br/>
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/302>302 Found</a><br/>
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/303>303 See Other</a><br/>
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/304>304 Not Modified</a><br/>
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/307>307 Temporary Redirect</a><br/>
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/308>308 Permanent Redirect</a><br/>
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/400>400 Bad Request</a><br/>
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/401>401 Unauthorized</a><br/>
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/402>402 Payment Required</a><br/>
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/403>403 Forbidden</a><br/>
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/404>404 Not Found</a><br/>
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/405>405 Method Not Allowed</a><br/>
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/406>406 Not Acceptable</a><br/>
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/407>407 Proxy Authentication Required</a><br/>
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/408>408 Request Timeout</a><br/>
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/409>409 Conflict</a><br/>
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/410>410 Gone</a><br/>
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/411>411 Length Required</a><br/>
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/412>412 Precondition Failed</a><br/>
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/413>413 Payload Too Large</a><br/>
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/414>414 URI Too Long</a><br/>
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/415>415 Unsupported Media Type</a><br/>
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/416>416 Range Not Satisfiable</a><br/>
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/417>417 Expectation Failed</a><br/>
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/418>418 I'm a teapot</a><br/>
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/422>422 Unprocessable Entity</a><br/>
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/425>425 Too Early</a><br/>
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/426>426 Upgrade Required</a><br/>
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/428>428 Precondition Required</a><br/>
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/429>429 Too Many Requests</a><br/>
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/431>431 Request Header Fields Too Large</a><br/>
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/451>451 Unavailable For Legal Reasons</a><br/>
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/500>500 Internal Server Error</a><br/>
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/501>501 Not Implemented</a><br/>
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/502>502 Bad Gateway</a><br/>
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/503>503 Service Unavailable</a><br/>
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/504>504 Gateway Timeout</a><br/>
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/505>505 HTTP Version Not Supported</a><br/>
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/506>506 Variant Also Negotiates</a><br/>
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/507>507 Insufficient Storage</a><br/>
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/508>508 Loop Detected</a><br/>
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/510>510 Not Extended</a><br/>
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/511>511 Network Authentication Required</a><br/>
     */
    public HttpResponseMessage produce(int code, String... args) {
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
        hrm.addHeader("Location", args[0]);
        return hrm;
    }

    /**
     * <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/302>302 Found</a>
     */
    private HttpResponseMessage produce302(String[] args) {
        if (args.length < 1) return produce500();
        HttpResponseMessage hrm = new HttpResponseMessage(302);
        hrm.addHeader("Location", args[0]);
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
