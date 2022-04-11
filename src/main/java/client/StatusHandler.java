package client;

import server.HttpResponseMessage;

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
    public HttpResponseMessage handle(HttpResponseMessage msg) {
        // todo: statusHandler
        return null;
    }
}
