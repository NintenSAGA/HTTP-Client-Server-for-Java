package util.parser.transdecode;

import exception.InvalidMessageException;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static util.consts.TransferEncoding.content_length;

public class ContentLengthStrategy extends TransDecodeStrategy{
    @Override
    public byte[] getBody(Map<String, String> headers) throws InvalidMessageException {
        String lenStr = headers.get(content_length);
        int len;
        try {
            len = Integer.parseInt(lenStr);
        } catch (NumberFormatException e) {
            throw new InvalidMessageException("invalid content-length [%s]".formatted(lenStr));
        }

        try {
            return reader.readNBytes(len);
        } catch (ExecutionException | TimeoutException | InterruptedException e) {
            e.printStackTrace();
            throw new InvalidMessageException("content error");
        }

    }
}
