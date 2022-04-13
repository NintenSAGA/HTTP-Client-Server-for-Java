package server;

import client.HttpRequestMessage;
import util.Log;
import util.MessageHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class HttpServer {
    private final static int DEFAULT_BACKLOG = 50;
    private final static boolean DEFAULT_LONG_CONNECTION = false;
    private final static int DEFAULT_TIMEOUT = -1; // no timeout

    private final InetAddress address;
    private final int port;
    private final ServerSocket serverSocket;
    private final TargetHandler handler;

    public HttpServer(String hostName, int port) throws IOException {
        this.address = InetAddress.getByName(hostName);
        this.port = port;
        this.handler = TargetHandler.getInstance();
        serverSocket = new ServerSocket(this.port, DEFAULT_BACKLOG, this.address);
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
        Log.debugServer("The server is starting up....");

        try {
            while (true) {
                Socket socket = serverSocket.accept();
                CompletableFuture.runAsync(() -> handleSocket(socket, longConnection, timeOut));
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
     * Should be packed in a Thread <br/>
     * Socket handler --> TargetHandler --> Output handler <br/>
     */
    private void handleSocket(Socket socket, boolean longConnection, int timeOut) {
        Log.debugSocket(socket, "Accepted");
        try {
            if (longConnection) {
                assert timeOut > 0;
                socket.setSoTimeout(timeOut);
                Log.debugSocket(socket, "Long connection enabled with timout %d".formatted(timeOut));
            }

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            PrintWriter pw = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream()));

            do {
                HttpRequestMessage requestMessage;
                try {
                    requestMessage = temporaryParser(br);
                    Log.debugSocket(socket, requestMessage.flatMessage());
                    HttpResponseMessage responseMessage = handler.handle(requestMessage);
                    pw.print(packUp(responseMessage));
                    pw.flush();
                } catch (SocketTimeoutException e) {
                    Log.debugSocket(socket, "Socket timeout");
                    longConnection = false;
                } catch (Exception e) {
                    e.printStackTrace();
                    pw.write(packUp(internalError()).flatMessage());
                    pw.flush();
                    longConnection = false;
                }
            } while (longConnection);

            br.close(); pw.close();
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
