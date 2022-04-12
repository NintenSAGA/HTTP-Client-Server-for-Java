package server;

import client.HttpRequestMessage;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer {
    private final static int DEFAULT_BACKLOG = 50;

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
     */
    public void launch() {
        // todo: main loop
    }

    /**
     * Should be packed in a Thread <br/>
     * Socket handler --> TargetHandler --> Output handler <br/>
     * @param timeOut referring to Socket.setSoTimeout()
     */
    private void handleSocket(Socket socket, boolean longConnection, int timeOut) {
        // todo request handler
    }

    /**
     * Convert raw string to HttpRequestMessage object <br/>
     * Referring <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Messages>Http Message</a>
     * @param raw raw string from socket input
     * @return HttpRequestMessage object
     */
    private static HttpRequestMessage ParseRequestMessage(String raw) {
        // todo: ParseRequestMessage
        return null;
    }
}
