package server;

import client.HttpRequestMessage;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import util.Log;
import util.MessageHelper;
import util.WebMethods;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * Singleton container of target mapping methods
 */
class TargetSet {
    @AllArgsConstructor
    private class User {
        @NonNull String name; @NonNull String password;

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

    private static final TargetSet instance = new TargetSet();
    private final ResponseMessageFactory factory;
    static TargetSet getInstance(){ return instance; }

    private final ConcurrentMap<String, User> userMap;
    private final ConcurrentMap<String, String> keyToName;
    private final String authKey = "Catherine";

    private TargetSet() {
        factory = ResponseMessageFactory.getInstance();
        userMap = new ConcurrentHashMap<>();
        keyToName = new ConcurrentHashMap<>();
    }

    private Map<String, String> parseArgs(String target) {
        int st = 0;
        while ( st < target.length() && target.charAt(st) != '?' ) st++;
        if (st == target.length()) return null;

        String argStr = target.substring(st + 1);
        try {
            return Arrays.stream(argStr.split("\\&"))
                            .map(s -> s.split("="))
                            .collect(Collectors.toMap(a -> a[0], a->a[1]));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Validate the login status
     * @return authCode if valid, or null
     */
    private String checkStatus(HttpRequestMessage msg) {
        Map<String, String> cookies = msg.getCookies();
        String code;
        if (cookies == null || (code = cookies.get("authCode")) == null || keyToName.get(code) == null)
            return null;
        return code;
    }

    /**
     * Login with name and password
     * @return new authCode
     * @throws LoginException containing exception message
     */
    private String login(String name, String password) throws LoginException {
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
    synchronized HttpResponseMessage status(HttpRequestMessage msg) {
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
    synchronized HttpResponseMessage login(HttpRequestMessage msg) {
        Map<String, String> args = parseArgs(msg.getTarget());
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
    synchronized HttpResponseMessage logout(HttpRequestMessage msg) {
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
    synchronized HttpResponseMessage register(HttpRequestMessage msg) {
        try {
            Map<String, String> argMap = parseArgs(msg.getTarget());
            if (argMap == null) throw new NullPointerException();

            String name = argMap.get("name"), password = argMap.get("password");

            HttpResponseMessage hrm;

            if (userMap.containsKey(name)) {
                hrm = factory.produce(200);
                hrm.setBodyAsPlainText("The name has already existed!");
                return hrm;
            }

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


    @Mapping(value = "Missing", method = {WebMethods.GET, WebMethods.POST})
    HttpResponseMessage responseOnMis(HttpRequestMessage msg) {
        Log.debug("Target not found!");
        // todo
        return factory.produce(404);
    }

    @Mapping(value = "/test", method = WebMethods.GET)
    HttpResponseMessage test(HttpRequestMessage msg) {
        HttpResponseMessage hrm = factory.produce(200);
        hrm.setBodyAsPlainText("逐梦演艺圈");
        return hrm;
    }

    @Mapping(value = "/moved", method = WebMethods.GET)
    HttpResponseMessage moved(HttpRequestMessage msg) {
        return factory.produce(301, "/test");
    }
}
