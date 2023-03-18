package edu.nju.http.server.target;

import edu.nju.http.message.HttpRequestMessage;
import edu.nju.http.message.HttpResponseMessage;
import edu.nju.http.message.MessageHelper;
import edu.nju.http.message.consts.WebMethods;
import edu.nju.http.util.Log;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class LoginSystem extends TargetSet {
  @AllArgsConstructor
  private static class User {
    @NonNull String name;
    @NonNull String password;

    String authCodeEncrypt() throws NoSuchAlgorithmException {
      byte[] b = MessageDigest.getInstance("SHA-256").digest((name + authKey + MessageHelper.getTime()).getBytes());
      BigInteger n = new BigInteger(1, b);
      return n.toString(16);
    }
  }

  private static class LoginException extends Exception {
    public LoginException(String message) {
      super(message);
    }
  }

  private static final ConcurrentMap<String, User> userMap;
  private static final ConcurrentMap<String, String> keyToName;
  private static final String authKey = "Catherine";

  static {
    userMap = new ConcurrentHashMap<>();
    keyToName = new ConcurrentHashMap<>();
  }

  /**
   * Validate the login status
   *
   * @return authCode if valid, or null
   */
  private static String checkStatus(HttpRequestMessage msg) {
    Map<String, String> cookies = msg.getCookies();
    String code;
    if (cookies == null || (code = cookies.get("authCode")) == null || keyToName.get(code) == null)
      return null;
    return code;
  }

  /**
   * Login with name and password
   *
   * @return new authCode
   * @throws LoginException containing exception message
   */
  private static String login(String name, String password) throws LoginException {
    User user = userMap.get(name);
    if (user == null)
      throw new LoginException("Invalid user name");
    if (!user.password.equals(password))
      throw new LoginException("Wrong password");
    try {
      String code = user.authCodeEncrypt();
      keyToName.put(code, name);
      return code;
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
      throw new LoginException("Error");
    }

  }

  @Mapping(value = "/status")
  public static synchronized HttpResponseMessage status(HttpRequestMessage msg) {
    HttpResponseMessage hrm = factory.produce(200);
    String code;
    if ((code = checkStatus(msg)) == null) {
      hrm.setBodyAsPlainText("Invalid cookie!");
    } else {
      User user = userMap.get(keyToName.get(code));
      hrm.setBodyAsPlainText("You've logged in as %s".formatted(user.name));
    }
    return hrm;
  }

  @Mapping(value = "/login", method = {WebMethods.GET})
  public static synchronized HttpResponseMessage login(HttpRequestMessage msg) {
    Map<String, String> args = MessageHelper.parseArgs(msg);
    HttpResponseMessage hrm = factory.produce(200);
    try {
      if (args == null) throw new Exception();
      String code = login(args.get("name"), args.get("password"));
      hrm.setBodyAsPlainText("Logged in successfully");
      hrm.addCookie("authCode", code);
    } catch (LoginException e) {
      hrm.setBodyAsPlainText(e.getMessage());
    } catch (Exception e) {
      hrm = factory.produce(400);
    }

    return hrm;
  }

  @Mapping(value = "/logout")
  public static synchronized HttpResponseMessage logout(HttpRequestMessage msg) {
    HttpResponseMessage hrm = factory.produce(200);
    String code = checkStatus(msg);
    if (code == null)
      hrm.setBodyAsPlainText("You haven't logged in yet");
    else {
      keyToName.remove(code);
      hrm.setBodyAsPlainText("You've logged out now");
    }
    return hrm;
  }

  @Mapping(value = "/register", method = {WebMethods.POST})
  public static synchronized HttpResponseMessage register(HttpRequestMessage msg) {
    try {
      Map<String, String> argMap = MessageHelper.parseArgs(msg);
      if (argMap == null) throw new NullPointerException();

      String name = argMap.get("name"), password = argMap.get("password");

      HttpResponseMessage hrm;

      if (userMap.containsKey(name)) {
        hrm = factory.produce(200);
        hrm.setBodyAsPlainText("The name has already existed!");
        return hrm;
      }

      Log.debug("User[%s] registered with password[%s]".formatted(name, password));

      User user = new User(name, password);
      userMap.put(name, user);
      String authCode = login(name, password);
      hrm = factory.produce(201);

      hrm.addCookie("authCode", authCode);
      hrm.setBodyAsPlainText("Registered successfully");

      return hrm;
    } catch (Exception e) {
      e.printStackTrace();
      return factory.produce(400);
    }
  }
}
