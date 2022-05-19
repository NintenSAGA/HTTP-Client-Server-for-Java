package client;

import server.HttpResponseMessage;
import util.Log;
import util.consts.WebMethods;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class HttpClient {
    private final InetAddress hostAddress;
    private final int hostPort;
    private final Socket socket;
    private final StatusHandler handler;
    private boolean longConnection;

    public HttpClient(String hostName, int hostPort, boolean longConnection) throws IOException {
        this.hostAddress = InetAddress.getByName(hostName);
        this.hostPort = hostPort;
        this.socket = new Socket(hostAddress, hostPort);
        this.handler = StatusHandler.getInstance();
        this.longConnection = longConnection;
        Log.logClient("Client has connect to the host");
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
        try (BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()))) {
                pw.print(request.flatMessage());
                pw.flush();
                Log.testInfo(br.readLine());
//            HttpResponseMessage httpResponseMessage = ParseResponseMessage(br);
//            httpResponseMessage = handler.handle(this, httpResponseMessage);
//            Log.testInfo(httpResponseMessage.flatMessage());
            }
        } catch (IOException e) {
            throw new IOException();
        }
        Log.logClient("Request complete");
        if (!longConnection)
            socket.close();
        return null;
    }

    /**
     * Convert raw string to HttpResponseMessage object <br/>
     * Referring <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Messages>Http Message</a>
     * @param br Buffered reader from requests
     * @return HttpResponseMessage object
     */
    private static HttpResponseMessage parseResponseMessage(BufferedReader br) {
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
