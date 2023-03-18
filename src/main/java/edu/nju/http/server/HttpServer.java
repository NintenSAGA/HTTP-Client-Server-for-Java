package edu.nju.http.server;

import edu.nju.http.exception.InvalidMessageException;
import edu.nju.http.message.HttpRequestMessage;
import edu.nju.http.message.HttpResponseMessage;
import edu.nju.http.message.MessageHelper;
import edu.nju.http.message.ResponseMessageFactory;
import edu.nju.http.message.packer.MessagePacker;
import edu.nju.http.message.parser.MessageParser;
import edu.nju.http.util.Config;
import edu.nju.http.util.Log;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeoutException;

import static edu.nju.http.message.consts.Headers.*;

public class HttpServer {

  private final
  AsynchronousServerSocketChannel aServerSocket;
  private final
  TargetHandler handler;
  private final
  Map<String, String> globalHeaders;
  private final
  Semaphore running;

  // ==================== Constructors ==================== //

  public HttpServer(String hostName, int port) throws IOException {
    this.handler = TargetHandler.getInstance();
    this.aServerSocket = AsynchronousServerSocketChannel.open();
    this.aServerSocket.bind(new InetSocketAddress(hostName, port));
    Log.logServer("Server bound to " + this.aServerSocket.getLocalAddress().toString());

    this.globalHeaders = new HashMap<>();

    this.running = new Semaphore(0);

    JSONObject jsonObject = Config.getConfigAsJsonObj(Config.GLOBAL_HEADERS);
    Log.debug(jsonObject.toString());
    jsonObject.keySet().forEach(k -> this.globalHeaders.put(k, jsonObject.getString(k)));
  }

  /**
   * Using default address: 127.0.0.1:8080
   */
  public HttpServer() throws IOException {
    this("127.0.0.1", 8080);
  }


  private static HttpResponseMessage badRequest() {
    return ResponseMessageFactory.getInstance().produce(400);
  }

  private static HttpResponseMessage internalError() {
    return ResponseMessageFactory.getInstance().produce(500);
  }

  /**
   * Start up the main loop <br/>
   * Should handle each new connection with a new thread
   *
   * @param keepAlive whether Keep-Alive is enabled
   * @param timeOut   timeout for long connection
   */
  public void launch(boolean keepAlive, int timeOut) {
    Log.logServer("The server is now running");

    try {
      aServerSocket.accept(null, new CompletionHandler<>() {

        @Override
        public void completed(AsynchronousSocketChannel result, Object attachment) {
          if (aServerSocket.isOpen())
            aServerSocket.accept(null, this);
          handleSocket(result, keepAlive, timeOut);
        }

        @Override
        public void failed(Throwable exc, Object attachment) {

        }
      });

      running.acquire();

    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  // ==================== Private ==================== //

  /**
   * Start up the main loop <br/>
   * Should handle each new connection with a new thread<br/>
   * Long connection is disabled by default
   */
  public void launch() {
    launch(Config.DEFAULT_KEEP_ALIVE, Config.DEFAULT_TIMEOUT);
  }

  /**
   * Shut down the server
   */
  public void shutdown() {
    Log.logServer("The server is going to shut down...");
    try {
      aServerSocket.close();
    } catch (IOException ignored) {

    }

    running.release();

    while (aServerSocket.isOpen())
      System.out.print("\tWaiting for shutdown...");

    Log.logServer("The server is down!");
  }

  /**
   * Socket handler
   * --> Message Parser
   * --> TargetHandler
   * --> Message Packer <br/>
   */
  private void handleSocket(AsynchronousSocketChannel socket, boolean keepAlive, int timeOut) {
    Log.logSocket(socket, "Accepted");

    try {
      socket.setOption(StandardSocketOptions.SO_SNDBUF, Config.SOCKET_BUFFER_SIZE);
      assert timeOut > 0;
      Log.logSocket(socket, "Keep-Alive %sabled with timout %d".formatted(keepAlive ? "en" : "dis", timeOut));


      do {
        HttpResponseMessage responseMessage = null;
        String acceptEncoding = "";

        try {

          // -------------------- 1. Receive and parse raw message to object -------------------- //
          MessageParser parser = new MessageParser(socket, timeOut);
          HttpRequestMessage requestMessage = parser.parseToHttpRequestMessage();

          Log.logSocket(socket, "Message received, target: " + requestMessage.getTarget());

          /*               Header setting               */
          acceptEncoding = requestMessage.getHeaderVal(ACCEPT_ENCODING);
          if (keepAlive && !KEEP_ALIVE.equals(requestMessage.getHeaderVal(CONNECTION))) {
            Log.logSocket(socket, "Keep-Alive disabled for the client");
            keepAlive = false;
          }

          // -------------------- 2. Handle the request and generate response -------------------- //
          responseMessage = handler.handle(requestMessage);

          /*               Error Handling               */
        } catch (TimeoutException e) {
          Log.logSocket(socket, "Socket timeout");
          keepAlive = false;
        } catch (InvalidMessageException e) {
          e.printStackTrace();
          Log.logSocket(socket, "Invalid message occurs");
          e.printMsg(socket);
          keepAlive = false;
          responseMessage = badRequest();
        } catch (Exception e) {
          e.printStackTrace();
          keepAlive = false;
          responseMessage = internalError();
        }
        /*               Error Handling               */

        // -------------------- 3. Pack up and send out the respond -------------------- //
        if (responseMessage != null) {
          if (keepAlive) responseMessage.addHeader(CONNECTION, KEEP_ALIVE);

          MessagePacker packer = new MessagePacker(
              packUp(responseMessage),
              Config.TRANSFER_ENCODINGS,
              acceptEncoding
          );
          packer.send(socket);
        }

      } while (keepAlive);

      socket.close();
      Log.logSocket(socket, "Connection closed");
    } catch (ExecutionException e) {
      Log.logServer("Connection interrupted: %s".formatted(e.getMessage()));
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
  }

  /**
   * Pack up the message before sending. Including appending the global header of the server.
   */
  private HttpResponseMessage packUp(HttpResponseMessage msg) {
    msg.putAllHeaders(this.globalHeaders);
    msg.addHeader("Date", MessageHelper.getTime());

    return msg;
  }

}
