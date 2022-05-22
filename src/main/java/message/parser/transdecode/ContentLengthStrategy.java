package message.parser.transdecode;

import exception.InvalidMessageException;
import util.Log;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static message.consts.Headers.content_length;

public class ContentLengthStrategy extends TransDecodeStrategy{
    @Override
    public byte[] getBody(Map<String, String> headers) throws InvalidMessageException {
        String lenStr = headers.get(content_length);
        headers.remove(content_length);
        int len;
        try {
            len = Integer.parseInt(lenStr);
        } catch (NumberFormatException e) {
            throw new InvalidMessageException("invalid content-length [%s]".formatted(lenStr));
        }

        try {
            Log.debug("Parsing %d bytes...".formatted(len));
            return reader.readNBytes(len);
        } catch (ExecutionException | TimeoutException | InterruptedException e) {
            e.printStackTrace();
            throw new InvalidMessageException("content error");
        }

    }
}
