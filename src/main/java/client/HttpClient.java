package client;

import exception.InvalidMessageException;
import lombok.Getter;
import server.HttpResponseMessage;
import util.Log;
import util.consts.WebMethods;
import util.packer.MessagePacker;
import util.parser.MessageParser;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLEncoder;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

public class HttpClient {
    private final
    StatusHandler handler;
    @Getter private final
    AsynchronousSocketChannel aSocket;
    @Getter private final
    String hostName;
    @Getter private final
    int hostPort;
    @Getter private final
    boolean longConnection;

    private
    Future<Void> connectedFuture;
    private
    boolean connected;

    // -------------------- Memorized for status handler -------------------- //
    @Getter private
    String method;
    @Getter private
    String param;
    @Getter private
    String body;
    @Getter private
    String[] headers;


    public HttpClient(String hostName, int hostPort, boolean longConnection) throws IOException {
        this.hostName = hostName;
        this.hostPort = hostPort;

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
       return request(WebMethods.GET, target, param, null, headers);
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
        return request(WebMethods.POST, target, param, body, headers);
    }

    HttpResponseMessage request(String method, String target, String param, String body, String ... headers) throws IOException {
        assert WebMethods.GET.equals(method) || WebMethods.POST.equals(method);
        assert target != null;

        /*               Memorized for status handler               */
        this.method = method;
        this.param = param;
        this.body = body;
        this.headers = headers;

        String nTarget = target;
        if (param != null
                && (WebMethods.GET.equals(method)
                    | (WebMethods.POST.equals(method) && body != null)))
            nTarget += "?" + URLEncoder.encode(param, StandardCharsets.UTF_8);

        HttpRequestMessage hrm = new HttpRequestMessage(method, nTarget);

        if (WebMethods.POST.equals(method)) {
            if (body != null)
                hrm.setBodyAsPlainText(body);
            else if (param != null)
                hrm.setBodyAsFormUrlencoded(param);
        }

        for (String header : headers) {
            String[] pair = header.split(": ");
            hrm.addHeader(pair[0], pair[1]);
        }

        return request(hrm);
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

            hrm = handler.handle(this, hrm);

            Log.logClient("Request complete");

            return hrm;
        } catch (InvalidMessageException e) {
            e.printMsg(aSocket);
            e.printStackTrace();
            Log.logClient("Parsing failed!");
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            Log.logClient("Sending failed!");
            throw new IOException();
        } catch (TimeoutException e) {
            e.printStackTrace();
            Log.logClient("Parsing failed!");
        }

        return null;
    }
}
