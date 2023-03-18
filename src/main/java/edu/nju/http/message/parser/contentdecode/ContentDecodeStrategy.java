package edu.nju.http.message.parser.contentdecode;

import java.util.Map;

public interface ContentDecodeStrategy {

  public byte[] getBody(Map<String, String> headers, byte[] bytes);
}
