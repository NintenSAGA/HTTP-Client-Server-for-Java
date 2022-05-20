package server;

import client.HttpRequestMessage;
import exception.InvalidMessageException;
import org.json.JSONObject;
import util.Config;
import util.HttpMessage;
import util.Log;
import util.MessageHelper;
import util.packer.MessagePacker;
import util.parser.MessageParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import static util.consts.TransferEncoding.CHUNKED;
import static util.consts.TransferEncoding.CONTENT_LENGTH;

public class HttpServer {
    private final static boolean    DEFAULT_LONG_CONNECTION = false;
    private final static int        DEFAULT_TIMEOUT         = 10000;
    private final static String[]   TRANSFER_ENCODINGS = { CHUNKED };
//    private final static String[]   TRANSFER_ENCODINGS = null;

    private final AsynchronousServerSocketChannel aServerSocket;
    private final TargetHandler handler;
    private final Map<String, String> globalHeaders;

    private final AtomicBoolean alive;

    // ==================== Constructors ==================== //

    public HttpServer(String hostName, int port) throws IOException {
        this.handler = TargetHandler.getInstance();
        this.aServerSocket = AsynchronousServerSocketChannel.open();
        this.aServerSocket.bind(new InetSocketAddress(hostName, port));
        this.globalHeaders = new HashMap<>();
        this.alive = new AtomicBoolean(true);

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

    // ==================== Public ==================== //

    /**
     * Start up the main loop <br/>
     * Should handle each new connection with a new thread
     * @param longConnection whether long connection is enabled
     * @param timeOut timeout for long connection
     */
    public void launch(boolean longConnection, int timeOut) {
        Log.logServer("The server is starting up....");

        try {
            while (true) {
                aServerSocket.accept(null, new CompletionHandler<>() {

                    @Override
                    public void completed(AsynchronousSocketChannel result, Object attachment) {
                        if (aServerSocket.isOpen())
                            aServerSocket.accept(null, this);
                        handleSocket(result, longConnection, timeOut);
                    }

                    @Override
                    public void failed(Throwable exc, Object attachment) {

                    }
                });
                System.in.read();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Start up the main loop <br/>
     * Should handle each new connection with a new thread<br/>
     * Long connection is disabled by default
     */
    public void launch() {
        launch(DEFAULT_LONG_CONNECTION, DEFAULT_TIMEOUT);
    }

    /**
     * Shut down the server
     */
    public void shutdown() {
        this.alive.set(false);
        Log.logServer("The server is going to shut down...");
        try {
            aServerSocket.close();
        } catch (IOException ignored) {

        } finally {
            Log.logServer("The server is down");
        }
    }

    // ==================== Private ==================== //

    /**
     * Should be packed in a Thread <br/>
     * Socket handler --> TargetHandler --> Output handler <br/>
     */
    private void handleSocket(AsynchronousSocketChannel socket, boolean longConnection, int timeOut) {
        Log.logSocket(socket, "Accepted");
        try {
            assert timeOut > 0;
            Log.logSocket(socket, "Long connection %sabled with timout %d".formatted(longConnection ? "en" : "dis", timeOut));


            do {
                HttpResponseMessage responseMessage = null;

                try {

                // -------------------- 1. Receive and parse raw message to object -------------------- //
                    MessageParser parser = new MessageParser(socket, timeOut);
                    HttpRequestMessage requestMessage = parser.parseToHttpRequestMessage();

                    Log.logSocket(socket, "Message received, target: " + requestMessage.getTarget());

                // -------------------- 2. Handle the request and generate response -------------------- //
                    responseMessage = handler.handle(requestMessage);

                /*               Error Handling               */
                } catch (TimeoutException e) {
                    Log.logSocket(socket, "Socket timeout");
                    longConnection = false;
                } catch (InvalidMessageException e) {
                    e.printStackTrace();
                    Log.logSocket(socket, "Invalid message occurs");
                    e.printMsg(socket);
                    longConnection = false;
                    responseMessage = badRequest();
                } catch (Exception e) {
                    e.printStackTrace();
                    longConnection = false;
                    responseMessage = internalError();
                }
                /*               Error Handling               */

                // -------------------- 3. Pack up and send out the respond -------------------- //
                if (responseMessage != null) {
                    MessagePacker packer = new MessagePacker(packUp(responseMessage), TRANSFER_ENCODINGS);
                    packer.send(socket);
                }

            } while (longConnection);

            Log.logSocket(socket, "Connection closed");
            socket.close();
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private static HttpResponseMessage internalError() {
        return ResponseMessageFactory.getInstance().produce(500);
    }

    private static HttpResponseMessage badRequest() {
        return ResponseMessageFactory.getInstance().produce(400);
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
