package client;

import exception.InvalidMessageException;
import server.HttpResponseMessage;
import util.Config;
import util.Log;
import util.consts.Headers;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
        Log.logClient("Status code received: %d".formatted(statusCode));

        try {
            Method m = this.getClass().getDeclaredMethod("handle" + statusCode, HttpClient.class, HttpResponseMessage.class);

            return (HttpResponseMessage) m.invoke(this, client, msg);

        } catch (NoSuchMethodException e) {
            Log.logClient("Handle returned directly...");
        } catch (InvocationTargetException | IllegalAccessException e) {
            Log.logClient("Handling failed...");
            System.err.println(e.getMessage());
        }

        return msg;
    }

    private HttpResponseMessage redirect(HttpClient client, HttpResponseMessage msg)
            throws IOException, InvalidMessageException {
        HttpClient nextClient;
        if (client.isLongConnection() && client.getASocket().isOpen())
            nextClient = client;
        else
            nextClient = new HttpClient(client.getHostName(), client.getHostPort(), client.isLongConnection());

        String nextTarget = msg.getHeaderVal(Headers.LOCATION);
        if (nextTarget == null) throw new InvalidMessageException("Invalid location");

        try {
            return redirect(nextTarget, client, nextClient, msg);
        } catch (IOException e) {
            Log.logClient("fall back to long connection disabled");
            nextClient = new HttpClient(client.getHostName(), client.getHostPort(), client.isLongConnection());
            return redirect(nextTarget, client, nextClient,msg);
        }
    }

    private HttpResponseMessage redirect(String nextTarget, HttpClient client, HttpClient nextClient, HttpResponseMessage msg)
            throws IOException {
        return nextClient.request(client.getMethod(), nextTarget, client.getParam(), client.getBody(), client.getHeaders());
    }

    /**
     * 301 Moved Permanently
     */
    private HttpResponseMessage handle301(HttpClient client, HttpResponseMessage msg)
            throws InvalidMessageException, IOException {
        return redirect(client, msg);
    }

    /**
     * 302 Found
     */
    private HttpResponseMessage handle302(HttpClient client, HttpResponseMessage msg)
            throws InvalidMessageException, IOException {
        return redirect(client, msg);
    }

    /**
     * 304 Not Modified
     */
    private HttpResponseMessage handle304(HttpClient client, HttpResponseMessage msg) {
        msg.loadBodyFromCache(Config.CLIENT_CACHE, client.getHostName() + client.getRawTarget());
        return msg;
    }
}
