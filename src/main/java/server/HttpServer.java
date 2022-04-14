package server;

import client.HttpRequestMessage;
import org.json.JSONObject;
import util.Config;
import util.HttpMessage;
import util.Log;
import util.MessageHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class HttpServer {
    private final static int        DEFAULT_BACKLOG = 50;
    private final static boolean    DEFAULT_LONG_CONNECTION = false;
    private final static int        DEFAULT_TIMEOUT = 10000;

    private final InetAddress address;
    private final int port;
    private final ServerSocket serverSocket;
    private final TargetHandler handler;
    private final Map<String, String> globalHeaders;

    private final AtomicBoolean alive;

    public HttpServer(String hostName, int port) throws IOException {
        this.address = InetAddress.getByName(hostName);
        this.port = port;
        this.handler = TargetHandler.getInstance();
        this.serverSocket = new ServerSocket(this.port, DEFAULT_BACKLOG, this.address);
        this.globalHeaders = new HashMap<>();
        this.alive = new AtomicBoolean(true);

        JSONObject jsonObject = Config.getConfigAsJsonObj(Config.GLOBAL_HEADERS);
        Log.debug(jsonObject.toString());
        jsonObject.keySet().forEach(k -> this.globalHeaders.put(k, jsonObject.getString(k)));

        this.serverSocket.setSoTimeout(10000);
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
            while (alive.get()) {
                try {
                    Socket socket = serverSocket.accept();
                    CompletableFuture.runAsync(() -> handleSocket(socket, longConnection, timeOut));
                } catch (SocketTimeoutException ignored) { }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.logServer("The server is down");
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
    }

    /**
     * Should be packed in a Thread <br/>
     * Socket handler --> TargetHandler --> Output handler <br/>
     */
    private void handleSocket(Socket socket, boolean longConnection, int timeOut) {
        Log.logSocket(socket, "Accepted");
        try {
            if ("keep-alive".equals(globalHeaders.get("Connection")))
                socket.setKeepAlive(true);  // Keep Alive

            assert timeOut > 0;
            socket.setSoTimeout(timeOut);
            Log.logSocket(socket, "Long connection %sabled with timout %d".formatted(longConnection ? "en" : "dis", timeOut));

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            PrintWriter pw = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream()));
            OutputStream outputStream = socket.getOutputStream();

            do {
                HttpRequestMessage requestMessage;
                try {
                    requestMessage = temporaryParser(br);
                    Log.logSocket(socket, "Message received, target: " + requestMessage.getTarget());
                    HttpResponseMessage responseMessage = handler.handle(requestMessage);
                    if (responseMessage.isBodyBinary()) {
                        outputStream.write(packUp(responseMessage).flatMessageToBinary());
                        outputStream.flush();
                    } else {
                        pw.print(packUp(responseMessage));
                        pw.flush();
                    }
                } catch (SocketTimeoutException e) {
                    Log.logSocket(socket, "Socket timeout");
                    longConnection = false;
                } catch (Exception e) {
                    e.printStackTrace();
                    pw.write(packUp(internalError()).flatMessage());
                    pw.flush();
                    longConnection = false;
                }
            } while (longConnection);

            Log.logSocket(socket, "Connection closed");
            br.close(); pw.close(); outputStream.close();
            socket.close();
        } catch (IOException e) {
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
        assert start.length == 3;

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
