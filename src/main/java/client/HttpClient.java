package client;

import exception.InvalidMessageException;
import server.HttpResponseMessage;
import util.Log;
import util.consts.WebMethods;
import util.packer.MessagePacker;
import util.parser.MessageParser;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

public class HttpClient {
    private final AsynchronousSocketChannel aSocket;
    private final StatusHandler handler;
    private final String hostName;
    private boolean longConnection;
    private Future<Void> connectedFuture;
    private boolean connected;

    public HttpClient(String hostName, int hostPort, boolean longConnection) throws IOException {
        this.hostName = hostName;

        this.aSocket = AsynchronousSocketChannel.open();
        InetSocketAddress hostAddress = new InetSocketAddress(hostName, hostPort);
        connectedFuture = aSocket.connect(hostAddress);

        this.handler = StatusHandler.getInstance();
        this.longConnection = longConnection;

        this.connected = false;
    }

    public HttpClient(String hostName) throws IOException {
        this(hostName, 80, false);
    }

    /**
     * With long connection disabled
     */
    public HttpClient(String hostName, int hostPort) throws IOException {
        this(hostName, hostPort, false);
    }

    /**
     * With long connection disabled. Connect to 127.0.0.1:8080
     */
    public HttpClient() throws IOException {
        this("127.0.0.1", 8080);
    }

    /**
     * HTTP GET method
     * @param target    target, e.g.: /path/to/file
     * @param param     parameters, e.g.: user=sega&password=123
     * @param headers   headers, one expression per String, e.g.: "User-Agent: AbaAba/0.1".
     */
    public HttpResponseMessage get(String target, String param, String ... headers) throws IOException {
        HttpRequestMessage hrm = new HttpRequestMessage(WebMethods.GET, target + (param == null ? "" : "?" + param));
        return request(hrm, headers);
    }

    /**
     * HTTP POST method<br/>
     * Only support ascii body with Content-Length
     * @param target    target, e.g.: /path/to/file
     * @param param     parameters, e.g.: user=sega&password=123
     * @param body      plain text body. Use null for empty body (and param as application/x-www-form-urlencoded)
     * @param headers   headers, one expression per String, e.g.: "User-Agent: AbaAba/0.1".
     */
    public HttpResponseMessage post(String target, String param, String body, String ... headers) throws IOException {
        HttpRequestMessage hrm;
        if (body == null) {
            hrm = new HttpRequestMessage(WebMethods.POST, target);
            if (param != null)
                hrm.setBodyAsFormUrlencoded(param);
        } else {
            hrm = new HttpRequestMessage(WebMethods.POST, target + (param == null ? "" : "?" + param));
            hrm.setBodyAsPlainText(body);
        }
        return request(hrm, headers);
    }

    private HttpResponseMessage request(HttpRequestMessage request, String ... headers) throws IOException {
        for (String header : headers) {
            String[] pair = header.split(": ");
            request.addHeader(pair[0], pair[1]);
        }
        return request(request);
    }

    private HttpResponseMessage request(HttpRequestMessage request) throws IOException {
        try {
            if (!connected) {
                connectedFuture.get();
                Log.logClient("Client has connect to the host");
                connected = true;
            }

            request.addHeader("Host", hostName);

            MessagePacker packer = new MessagePacker(request, null);
            packer.send(aSocket);

            Log.debug(request.flatMessage());

            MessageParser parser = new MessageParser(aSocket, 10000);
            HttpResponseMessage hrm = parser.parseToHttpResponseMessage();

//            Log.testInfo(hrm.flatMessage());

            Log.logClient("Request complete");

            return hrm;
        } catch (InvalidMessageException e) {
            e.printMsg(aSocket);
            e.printStackTrace();
            Log.logClient("Parsing failed!");
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            Log.logClient("Sending failed!");
        } catch (TimeoutException e) {
            e.printStackTrace();
            Log.logClient("Parsing failed!");
        }

        return null;
    }

    /**
     * Show the response message in either CLI or GUI
     */
    private void showResponse(HttpResponseMessage responseMessage) {
        // todo: showResponse
    }
}
