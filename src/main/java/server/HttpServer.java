package server;

import client.HttpRequestMessage;
import org.json.JSONObject;
import util.Config;
import util.HttpMessage;
import util.Log;
import util.MessageHelper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public class HttpServer {
    private final static boolean    DEFAULT_LONG_CONNECTION = false;
    private final static int        DEFAULT_TIMEOUT         = 10000;
    private final static int        TRANSPORT_CHUNK_SIZE    = 1 << 10;

//    private final ServerSocket serverSocket;
    private final AsynchronousServerSocketChannel aServerSocket;
    private final TargetHandler handler;
    private final Map<String, String> globalHeaders;

    private final AtomicBoolean alive;

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

//        try {
//            while (alive.get()) {
//                try {
//                    a.se
//                    Socket socket = serverSocket.accept();
//                    CompletableFuture.runAsync(() -> handleSocket(socket, longConnection, timeOut));
//                } catch (SocketTimeoutException ignored) { }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        Log.logServer("The server is down");
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

    /**
     * Should be packed in a Thread <br/>
     * Socket handler --> TargetHandler --> Output handler <br/>
     */
    private void handleSocket(AsynchronousSocketChannel socket, boolean longConnection, int timeOut) {
        Log.logSocket(socket, "Accepted");
        try {
            assert timeOut > 0;
            Log.logSocket(socket, "Long connection %sabled with timout %d".formatted(longConnection ? "en" : "dis", timeOut));

            ByteBuffer inputByteBuffer = ByteBuffer.allocate(1 << 20);
            BufferedReader br = null;

            do {
                Log.debug("A new loop~");
                HttpRequestMessage requestMessage;
                ByteBuffer ans;
                try {
                    Future<Integer> future = socket.read(inputByteBuffer);
                    future.get(timeOut, TimeUnit.MILLISECONDS);
                    br = new BufferedReader(
                            new InputStreamReader(new ByteArrayInputStream(inputByteBuffer.array())));
                    requestMessage = temporaryParser(br);
                    inputByteBuffer.clear();

                    Log.logSocket(socket, "Message received, target: " + requestMessage.getTarget());
                    HttpResponseMessage responseMessage = handler.handle(requestMessage);
                    if (responseMessage.isBodyBinary()) {
                        ans = ByteBuffer.wrap(packUp(responseMessage).flatMessageToBinary());
                    } else {
                        ans = ByteBuffer.wrap(packUp(responseMessage).flatMessage().getBytes());
                    }

                    int written, chunk = TRANSPORT_CHUNK_SIZE;
                    for (written = 0; written < ans.limit(); ) {
                        written += socket.write(ans.slice(written, Math.min(ans.limit() - written, chunk))).get();
                    }
                    Log.logSocket(socket,"Response sent %f KB ".formatted((double) written / chunk));
                    Log.testExpect("Bytes sent", ans.limit(), written);
                } catch (TimeoutException | ExecutionException e) {
                    Log.logSocket(socket, "Socket timeout");
                    longConnection = false;
                } catch (Exception e) {
                    e.printStackTrace();
                    ans = ByteBuffer.wrap(packUp(internalError()).flatMessage().getBytes());
                    socket.write(ans).get();
                    longConnection = false;
                }
            } while (longConnection);

            Log.logSocket(socket, "Connection closed");
            if (br != null) br.close();
            socket.close();
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private static HttpResponseMessage internalError() {
        return ResponseMessageFactory.getInstance().produce(500);
    }

    private static HttpRequestMessage temporaryParser(BufferedReader br)
            throws IOException {
        String line = br.readLine();
        if (line == null) throw new SocketTimeoutException();
        String[] start = line.split(" ");
        assert start.length == 3 : start;

        Map<String, String> headers = new HashMap<>();
        String body = "";

        while (!(line = br.readLine()).isEmpty()) {
            String[] a = line.split(": ");
            assert a.length == 2;
            String key = a[0], val = a[1];
            headers.put(key, val);
        }
        if (headers.containsKey("Content-Length"))
            body = MessageHelper.readBody(br, Integer.parseInt(headers.get("Content-Length")));

        return new HttpRequestMessage(start[0], start[1], start[2], headers, body);
    }

    /**
     * Pack up the message before sending. Including appending the global header of the server.
     */
    private HttpResponseMessage packUp(HttpResponseMessage msg) {
        msg.getHeaders().putAll(this.globalHeaders);
        msg.addHeader("Date", MessageHelper.getTime());
        return msg;
    }

    /**
     * Convert raw string to HttpRequestMessage object <br/>
     * Referring <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Messages>Http Message</a>
     * @param br Buffered reader from handleSocket
     * @return HttpRequestMessage object
     */
    private static HttpRequestMessage ParseRequestMessage(BufferedReader br) {
        // todo: ParseRequestMessage 李佳骏 邱兴驰
        return null;
    }
}
