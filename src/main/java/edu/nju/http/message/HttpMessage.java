package edu.nju.http.message;

import edu.nju.http.util.Config;
import edu.nju.http.util.Log;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.function.BiFunction;

import static edu.nju.http.message.consts.Headers.*;

public abstract class HttpMessage {
  public static final String HTTP10 = "HTTP/1";
  public static final String HTTP11 = "HTTP/1.1";
  public static final String HTTP20 = "HTTP/2";


  private static final Map<String, MediaType> suffixToMime;
  private static final Map<String, String> mimeToSuffix;

  static {
    suffixToMime = new HashMap<>();
    mimeToSuffix = new HashMap<>();

    JSONObject json = Config.getConfigAsJsonObj(Config.MIME);

    for (String codeType : json.keySet()) {
      JSONObject codeTypeJson = json.getJSONObject(codeType);
      /*               Each Code Type               */
      for (String type : codeTypeJson.keySet()) {
        /*               Each Type               */
        JSONObject temp = codeTypeJson.getJSONObject(type);
        temp.keySet().forEach(suffix -> {
          /*               Each Subtype               */
          String subtype = temp.getString(suffix);
          MediaType mediaType = new MediaType(type, subtype);

          suffixToMime.put(suffix, mediaType);
          mimeToSuffix.put(mediaType.toString(), suffix);

          if (codeType.equals("binary"))
            MediaType.BINARY_TYPE.add(mediaType);
        });
      }
    }
  }

  // ====================== Nested Class ========================= //

  @Data
  private static class MediaType {
    private final static Set<MediaType> BINARY_TYPE;

    static {
      BINARY_TYPE = new HashSet<>();
    }

    @NonNull String type;
    @NonNull String subtype;

    @Override
    public String toString() {
      return "%s/%s".formatted(type, subtype);
    }
  }

  @NonNull
  @Getter
  private final String httpVersion;
  @NonNull
  @Getter
  private final Map<String, String> headers;

  private InputStream bodyStream;

  // ====================== Public ========================= //

  public HttpMessage() {
    httpVersion = HTTP11;
    headers = new HashMap<>();
  }

  public HttpMessage(String httpVersion, Map<String, String> headers, byte[] body) {
    this.httpVersion = httpVersion;
    this.headers = headers;

    byte[] bytes = Objects.requireNonNullElseGet(body, () -> new byte[0]);
    bodyStream = new ByteArrayInputStream(bytes);
  }

  public HttpMessage(String httpVersion, Map<String, String> headers, String body) {
    this(httpVersion, headers, body.getBytes());
  }

  /**
   * Containing the trailing CRLF
   */
  public String getStartLineAndHeaders() {
    return
        getStartLine() + "\r\n" +
            getHeadersAsString() + "\r\n";
  }

  public InputStream getStartLineAndHeadersAsStream() {
    return new ByteArrayInputStream(
        getStartLineAndHeaders().getBytes());
  }

  public InputStream getBodyAsStream() {
    if (bodyStream != null) return bodyStream;
    else return new ByteArrayInputStream(new byte[0]);
  }

  public byte[] getBodyAsBytes() {
    try {
      return getBodyAsStream().readAllBytes();
    } catch (IOException e) {
      e.printStackTrace();
      return new byte[0];
    }
  }

  public String getBodyAsString() {
    return new String(getBodyAsBytes());
  }

  public String flatMessage() {
    return new String(flatMessageToBinary(), StandardCharsets.UTF_8);
  }

  public byte[] flatMessageToBinary() {
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      out.write(getStartLineAndHeaders().getBytes());
      out.write(getBodyAsBytes());
      return out.toByteArray();
    } catch (IOException e) {
      e.printStackTrace();
      return new byte[0];
    }
  }

  @Override
  public String toString() {
    return this.flatMessage();
  }

  // -------------------- Header Setting -------------------- //

  public void addHeader(String key, String val) {
    headers.put(key, val);
  }

  public void putAllHeaders(Map<String, String> headers) {
    this.headers.putAll(headers);
  }

  public String getHeaderVal(String key) {
    return headers.get(key.toLowerCase(Locale.ROOT));
  }

  public boolean containsHeader(String key) {
    return headers.containsKey(key.toLowerCase(Locale.ROOT));
  }

  public void mergeHeader(String a, String b, BiFunction<String, String, String> func) {
    headers.merge(a, b, func);
  }


  // -------------------- Body Setting -------------------- //
  public void setBody(String body) {
    setBody(body.getBytes());
  }

  public void setBody(byte[] body) {
    setBody(new ByteArrayInputStream(body), body.length);
  }

  public void setBody(InputStream stream, long length) {
    this.bodyStream = stream;
    addHeader(CONTENT_LENGTH, String.valueOf(length));
  }

  public void setBodyType(String type) {
    headers.put(CONTENT_TYPE, type);
  }

  public void setBodyWithType(byte[] body, String type) {
    setBodyType(type);
    setBody(body);
  }

  public void setBodyWithType(String body, String type) {
    setBodyWithType(body.getBytes(), type);
  }

  /**
   * Set body as plain text and calculate content-length automatically
   */
  public void setBodyAsPlainText(String body) {
    setBodyWithType(body, "%s; %s".formatted(TEXT_PLAIN, CHARSET_UTF8));
  }

  private void setBodyAsFile(String path, long fileSize, InputStream stream) {
    String[] a = path.split("\\.");
    String suffix = a[a.length - 1];
    MediaType mediaType = suffixToMime.getOrDefault(suffix, suffixToMime.get("default"));
    Log.debug("File %s sent as %s".formatted(path, mediaType));

    String type;
    if (MediaType.BINARY_TYPE.contains(mediaType)) {
      Log.debug(mediaType, " is binary");
      type = "%s".formatted(mediaType);
    } else {
      type = "%s; charset=UTF-8".formatted(mediaType);
    }

    String size;
    if (fileSize >= (1 << 20)) {
      size = "%.2fMB".formatted((double) fileSize / (1 << 20));
    } else {
      size = "%.2fKB".formatted((double) fileSize / (1 << 10));
    }

    String pathS = path.replace(Config.USER_DIR, ".");

    Log.logPrompt("File packed", "[%s][%s]".formatted(pathS, size));

    setBodyType(type);
    setBody(stream, fileSize);
  }

  public void setBodyAsFileWithAbsPath(Path path) {
    try {
      String time = Config.getResourceLastModifiedTime(path);
      long fileSize = Config.getSizeOfResource(path);
      InputStream stream = Files.newInputStream(path);

      setBodyAsFile(path.toString(), fileSize, stream);
    } catch (IOException e) {
      e.printStackTrace();
      Log.debug("Setting failed!");
    }

  }

  public void setBodyAsFile(String relativePath) {
    Path path = Path.of(Config.STATIC_DIR, relativePath);
    Log.debug("Path: ", path);
    setBodyAsFileWithAbsPath(path);
  }

  public static Path getCachePathParent(String cacheDir, String file) {
    file = file.trim();
    while (file.startsWith("/")) file = file.substring(1);
    if (file.length() == 0 || file.endsWith("/")) file += "index";

    return Path.of(cacheDir, file);
  }

  public static Path getCachePath(String cacheDir, String file, String type) {
    String typeName = type.split(";")[0].strip();
    String suffix = mimeToSuffix.getOrDefault(typeName, "bin");

    Path cachePathParent = getCachePathParent(cacheDir, file);

    return Path.of(cachePathParent.toString(), "cache." + suffix);
  }

  public static Path getCache(String cacheDir, String file) {
    Path fPath = getCachePathParent(cacheDir, file);
    if (Files.exists(fPath)) {
      try {
        var optional = Files.list(fPath)
            .filter(p -> p.toString().matches(".*/cache\\..*$"))
            .findFirst();
        if (optional.isPresent()) {
          Log.debug("Cache hit!");
          var path = optional.get();
          Log.debug("Cache path: %s".formatted(path.toString()));
          return path;
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    Log.debug("Cache miss!");
    return null;
  }

  public void storeBodyInCache(String cacheDir, String file, String type) {
    Path fPath = getCachePath(cacheDir, file, type);

    byte[] bytes = getBodyAsBytes();
    int length = bytes.length;
    try {
      Files.createDirectories(fPath.getParent());
      Files.write(
          fPath,
          bytes,
          StandardOpenOption.TRUNCATE_EXISTING,
          StandardOpenOption.CREATE
      );
      setBody(new FileInputStream(fPath.toFile()), length);
      Log.debug("Body cached");
    } catch (IOException e) {
      e.printStackTrace();
      Log.debug("Body cache setting failed! Fall back");
      setBody(bytes);
    }
  }

  public void loadBodyFromCache(String cacheDir, String file) {
    Path fPath = getCache(cacheDir, file);
    setBodyAsFileWithAbsPath(fPath);
  }

  // ====================== Protected ========================= //

  protected abstract String getStartLine();

  // ==================== Private ==================== //

  private String getHeadersAsString() {
    StringBuilder sb = new StringBuilder();
    headers.forEach((k, v) -> sb.append("%s: %s\r\n".formatted(k, v)));
    return sb.toString();
  }

}
