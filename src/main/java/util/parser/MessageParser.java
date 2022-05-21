package util.parser;

import client.HttpRequestMessage;
import exception.InvalidMessageException;
import server.HttpResponseMessage;
import util.Log;
import util.parser.contentdecode.ContentDecodeStrategy;
import util.parser.contentdecode.GzipStrategy;
import util.parser.transdecode.ChunkedStrategy;
import util.parser.transdecode.ContentLengthStrategy;
import util.parser.transdecode.TransDecodeStrategy;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static util.consts.ContentEncodings.content_encoding;
import static util.consts.TransferEncoding.*;


public class MessageParser {
    private final static
    int BUFFER_CAP = 1 << 20;
    private final static
    Map<String, TransDecodeStrategy> transDecodeStrategyMap;
    static {
        transDecodeStrategyMap = new HashMap<>();
        transDecodeStrategyMap.put( content_length,    new ContentLengthStrategy() );
        transDecodeStrategyMap.put( CHUNKED,           new ChunkedStrategy() );
    }

    private final static
    Map<String, ContentDecodeStrategy> contDecodeStrategyMap;
    static {
        contDecodeStrategyMap = new HashMap<>();
        contDecodeStrategyMap.put( GZIP, new GzipStrategy() );
    }


    private
    final Map<String, String> headers;
    private
    final CustomizedReader customizedReader;

    private
    String[] startLine;
    private
    byte[] body;


    // ================= Constructor ======================== //

    /**
     * Used for testing
     */
    public MessageParser(byte[] bytes) {
        this.headers = new HashMap<>();
        this.customizedReader = new CustomizedReader(bytes);
    }

    public MessageParser(AsynchronousSocketChannel socket, int timeout) {
        this.headers = new HashMap<>();
        this.customizedReader = new CustomizedReader(socket, ByteBuffer.allocate(BUFFER_CAP), timeout);
    }

    // ================= Public ======================== //

    public HttpRequestMessage parseToHttpRequestMessage()
            throws ExecutionException, InterruptedException,
            TimeoutException, InvalidMessageException {
        parse();
        return new HttpRequestMessage(startLine[0], startLine[1], startLine[2], headers, body);
    }

    public HttpResponseMessage parseToHttpResponseMessage()
            throws ExecutionException, InterruptedException,
            TimeoutException, NumberFormatException, InvalidMessageException
    {
        parse();
        return new HttpResponseMessage(startLine[0], startLine[1], startLine[2], headers, body);
    }

    // ================= Private ======================== //

    /**
     * All the key-val pairs of headers will be transformed to lower-case
     */
    private void parse()
            throws ExecutionException, InterruptedException,
            TimeoutException, InvalidMessageException {
        // -------- 1. Start Line ----------- //
        String line = customizedReader.readLine();

        startLine = line.split(" ");
        if (startLine.length != 3) {
            throw new InvalidMessageException("header line: ", line);
        }

        // -------- 2. Headers -------------- //

        while (!(line = customizedReader.readLine()).isEmpty()) {
            int colonIdx = line.indexOf(':');
            if (colonIdx == -1) {
                throw new InvalidMessageException("header line: ", line);
            }
            String key = line.substring(0, colonIdx), val = line.substring(colonIdx + 1);
            headers.put(key.toLowerCase(Locale.ROOT).strip(), val.strip());
        }

        // -------- 3. Body (Opt) ----------- //

        transferDecode();


        if (body == null) body = new byte[0];
        else {
            contentDecode();
        }

        assert startLine != null;
    }

    private void transferDecode() throws InvalidMessageException {
        TransDecodeStrategy strategy = null;

        // -------------------- Content-Length -------------------- //
        if (headers.containsKey(content_length)) {
            strategy = transDecodeStrategyMap.get(content_length);
            strategy.init(customizedReader);
            body = strategy.getBody(headers);
        }

        // -------------------- Others -------------------- //
        if (headers.containsKey(transfer_encoding)) {
            String formatStr = headers.get(transfer_encoding);
            String[] formats = formatStr.split(",");

            for (int i = formats.length - 1; i >= 0; i--) {
                String format = formats[i].strip();

                if (!transDecodeStrategyMap.containsKey(format.toLowerCase(Locale.ROOT))) {
                    Log.panic("Unimplemented!");
                    return;
                }

                strategy = transDecodeStrategyMap.get(format.toLowerCase(Locale.ROOT));
                if (body == null) {
                    strategy.init(customizedReader);
                } else {
                    strategy.init(body);
                }

                body = strategy.getBody(headers);
            }

        }
    }

    private void contentDecode() {
        if (!headers.containsKey(content_encoding)) return;

        String formatStr = headers.get(content_encoding);
        String[] formats = formatStr.split(",");

        for (int i = formats.length - 1; i >= 0; i--) {
            String format = formats[i].strip();

            if (!contDecodeStrategyMap.containsKey(format.toLowerCase(Locale.ROOT))) {
                Log.panic("Unimplemented!");
                return;
            }

            ContentDecodeStrategy strategy = contDecodeStrategyMap.get(format.toLowerCase(Locale.ROOT));
            body = strategy.getBody(headers, body);
        }
    }

}
