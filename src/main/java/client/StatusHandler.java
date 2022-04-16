package client;

import server.HttpResponseMessage;

import java.io.IOException;

/**
 * A singleton handler
 */
public class StatusHandler {
    private static final StatusHandler instance = new StatusHandler();

    private StatusHandler() {}

    public static StatusHandler getInstance() {
        return instance;
    }

    /**
     * Perform the operation according to the status code
     * @return New response message after the operation, or the original one.
     */
    public HttpResponseMessage handle(HttpClient client, HttpResponseMessage msg) throws IOException {
        int statusCode = msg.getStatusCode();
        switch (statusCode){
            case 301:
                // Moved Permanently
            case 302: {
                // Found
                String httpVersion = msg.getHttpVersion();
                String newTarget = msg.getHeaders().get("Location");
                return client.get(newTarget, null);
            }
            case 304: {
                // Not Modified
                // todo: 从客户端缓存中读取数据
                return msg;
            }
            default:
                return msg;
        }
        // todo: statusHandler 陈骏
    }
}
