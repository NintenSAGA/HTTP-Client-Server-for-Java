package util.parser.transdecode;

import exception.InvalidMessageException;

import java.util.Map;

public class GzipStrategy extends TransDecodeStrategy {
    @Override
    public byte[] getBody(Map<String, String> headers) throws InvalidMessageException {
        return new byte[0];
    }
}
