package edu.nju.http.client;

import edu.nju.http.exception.InvalidMessageException;
import edu.nju.http.message.HttpResponseMessage;
import edu.nju.http.message.consts.Headers;
import edu.nju.http.util.Config;
import edu.nju.http.util.Log;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * A singleton handler
 */
public class StatusHandler {
  private static final StatusHandler instance = new StatusHandler();

  private StatusHandler() {
  }

  public static StatusHandler getInstance() {
    return instance;
  }

  /**
   * Perform the operation according to the status code
   *
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
    if (client.isKeepAlive() && client.getASocket().isOpen())
      nextClient = client;
    else
      nextClient = new HttpClient(client.getHostName(), client.getHostPort(), client.isKeepAlive());

    String nextUri = msg.getHeaderVal(Headers.LOCATION);
    if (nextUri == null) throw new InvalidMessageException("Invalid location");

    if (nextUri.startsWith("https")) {
      Log.logClient("Redirection to HTTPS is prohibited.");
      return msg;
    }

    try {
      return redirect(nextUri, client, nextClient, msg);
    } catch (IOException e) {
      Log.logClient("fall back to long connection disabled");
      nextClient = new HttpClient(client.getHostName(), client.getHostPort(), client.isKeepAlive());
      return redirect(nextUri, client, nextClient, msg);
    }
  }

  private HttpResponseMessage redirect(String nextTarget, HttpClient client, HttpClient nextClient, HttpResponseMessage msg)
      throws IOException {
    return nextClient.request(client.getMethod(), nextTarget, client.getBody(), client.getHeaders());
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
    msg.loadBodyFromCache(Config.CLIENT_CACHE, client.getHostName() + client.getRawPath());
    return msg;
  }
}
