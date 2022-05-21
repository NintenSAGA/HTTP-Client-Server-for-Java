package util.parser.contentdecode;

import java.util.Map;

public interface ContentDecodeStrategy {

    public byte[] getBody(Map<String, String> headers, byte[] bytes);
}
