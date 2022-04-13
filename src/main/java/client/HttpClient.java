package client;

import server.HttpResponseMessage;
import util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class HttpClient {
    private final InetAddress hostAddress;
    private final int hostPort;
    private final Socket socket;
    private final StatusHandler handler;

    public HttpClient(String hostName, int hostPort) throws IOException {
        this.hostAddress = InetAddress.getByName(hostName);
        this.hostPort = hostPort;
        this.socket = new Socket();
        handler = StatusHandler.getInstance();
    }

    /**
     * Using the default Host address: 127.0.0.1:8080
     */
    public HttpClient() throws IOException {
        this("127.0.0.1", 8080);
    }

    /**
     * This method is intended to be used for test and debug only
     */
    public void request(HttpRequestMessage request) {
        // todo: request 徐浩钦
        Log.logClient("Client can print debug message in this way");
    }

    // todo: other methods... 徐浩钦

    /**
     * Convert raw string to HttpResponseMessage object <br/>
     * Referring <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Messages>Http Message</a>
     * @param br Buffered reader from requests
     * @return HttpResponseMessage object
     */
    private static HttpResponseMessage ParseResponseMessage(BufferedReader br) {
        // todo: ParseResponseMessage 李佳骏 邱兴驰
        return null;
    }

    /**
     * Show the response message in either CLI or GUI
     */
    private void showResponse(HttpResponseMessage responseMessage) {
        // todo: showResponse
    }
}
