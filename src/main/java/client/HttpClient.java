package client;

import exception.InvalidMessageException;
import lombok.Getter;
import message.HttpRequestMessage;
import message.HttpResponseMessage;
import util.Config;
import message.HttpMessage;
import util.Log;
import message.consts.Headers;
import message.consts.WebMethods;
import message.packer.MessagePacker;
import message.parser.MessageParser;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.file.Path;
import java.util.Objects;
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
    boolean keepAlive;

    private
    Future<Void> connectedFuture;
    private
    boolean connected;

    // -------------------- Memorized for status handler -------------------- //
    @Getter private
    String method;
    @Getter private
    String query;
    @Getter private
    String body;
    @Getter private
    String[] headers;

    @Getter private
    String rawPath;

    public HttpClient(String hostName, int hostPort, boolean keepAlive) throws IOException {
        this.hostName = hostName;
        this.hostPort = hostPort;

        this.aSocket = AsynchronousSocketChannel.open();
        InetSocketAddress hostAddress = new InetSocketAddress(hostName, hostPort);
        connectedFuture = aSocket.connect(hostAddress);

        this.handler = StatusHandler.getInstance();
        this.keepAlive = keepAlive;

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
     * @param rawUri    Generic URI syntax
     * @param headers   headers, one expression per String, e.g.: "User-Agent: AbaAba/0.1".
     */
    public HttpResponseMessage get(String rawUri, String ... headers) throws IOException {
       return request(WebMethods.GET, rawUri, null, headers);
    }

    /**
     * HTTP POST method<br/>
     * Only support ascii body with Content-Length
     * @param rawUri    Generic URI syntax
     * @param body      plain text body. Use null for empty body (and param as application/x-www-form-urlencoded)
     * @param headers   headers, one expression per String, e.g.: "User-Agent: AbaAba/0.1".
     */
    public HttpResponseMessage post(String rawUri, String body, String ... headers) throws IOException {
        return request(WebMethods.POST, rawUri, body, headers);
    }

    HttpResponseMessage request(String method, String rawUri, String body, String ... headers) throws IOException {
        assert WebMethods.GET.equals(method) || WebMethods.POST.equals(method);
        assert rawUri != null;

        /*               Memorized for status handler               */
        URI uri;
        try {
            uri = new URI(rawUri);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new IOException();
        }
        this.method = method;
        this.rawPath = uri.getPath();
        this.query = uri.getQuery();
        this.body = body;
        this.headers = headers;

        String pathNQuery = this.rawPath;
        if (query != null
                && (WebMethods.GET.equals(method)
                    | (WebMethods.POST.equals(method) && body != null)))
            pathNQuery += "?" + query;

        HttpRequestMessage hrm = new HttpRequestMessage(method, pathNQuery);

        if (WebMethods.POST.equals(method)) {
            if (body != null)
                hrm.setBodyAsPlainText(body);
            else if (query != null)
                hrm.setBodyAsFormUrlencoded(query);
        }

        for (String header : headers) {
            int colonIdx = header.indexOf(':');
            String key = header.substring(0, colonIdx);
            String val = header.substring(colonIdx + 1);
            hrm.addHeader(key.trim(), val.trim());
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

            // -------------------- Header editing -------------------- //
            request.addHeader("Host", hostName);
            request.addHeader(Headers.USER_AGENT, "Wget/1.21.3");
            request.addHeader(Headers.ACCEPT_ENCODING, "gzip");
            request.addHeader(Headers.ACCEPT, "*/*");

            if (keepAlive) request.addHeader(Headers.CONNECTION, Headers.KEEP_ALIVE);

            // -------------------- Cache checking -------------------- //
            checkCache(request);

            // -------------------- Pack and send -------------------- //
            MessagePacker packer = new MessagePacker(request, null);
            packer.send(aSocket);

            /*               Output 1               */
            System.out.println(present(request));

            // -------------------- Receive and parse -------------------- //
            MessageParser parser = new MessageParser(aSocket, 10000);
            HttpResponseMessage hrm = parser.parseToHttpResponseMessage();

            // -------------------- Status Handler -------------------- //
            hrm = handler.handle(this, hrm);

            Log.logClient("Request complete");

            // -------------------- Caching -------------------- //
            String content_type = hrm.getHeaderVal(Headers.CONTENT_TYPE);
            if (    hrm.getStatusCode() != 304
                    && content_type != null
                    && !content_type.contains(Headers.TEXT_PLAIN)) {
                hrm.storeBodyInCache(Config.CLIENT_CACHE, getHostName() + this.getRawPath(), content_type);
            }

            // Returned without reading to avoid body stream being read out
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

    private void checkCache(HttpRequestMessage hrm) {
        Path path = HttpMessage.getCache(Config.CLIENT_CACHE, getHostName() + this.getRawPath());
        if (path != null) {
            String time = Config.getResourceLastModifiedTime(path);
            Log.debug("Cache last modified: ", time);
            hrm.addHeader(Headers.IF_MODIFIED_SINCE, time);
        }
    }

    public String present(HttpMessage hrm) {
        StringBuilder sb = new StringBuilder();
        sb.append(hrm.getStartLineAndHeaders());
        var ct = hrm.getHeaderVal(Headers.CONTENT_TYPE);
        if (ct != null) {
            ct = ct.trim();
            if (ct.startsWith(Headers.TEXT_PLAIN)) {
                sb.append(hrm.getBodyAsString());
            } else {
                sb.append("Body saved at: \n");

                String p = Objects.requireNonNull(HttpMessage.getCache(
                        Config.CLIENT_CACHE, getHostName() + this.getRawPath()))
                        .toString();

                sb.append("file:///").append(p);
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    public String present(HttpRequestMessage hrm) {
        return "\n>> ==================== HTTP Request Message ==================== <<\n" +
                present((HttpMessage) hrm);
    }

    public String present(HttpResponseMessage hrm) {
        return "\n<< ==================== HTTP Response Message ==================== >>\n" +
                present((HttpMessage) hrm);
    }
}
