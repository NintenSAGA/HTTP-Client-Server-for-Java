package server;

import client.HttpRequestMessage;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import util.Log;
import util.WebMethods;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Singleton container of target mapping methods
 */
class TargetSet {
    @AllArgsConstructor
    private class User {
        @NonNull String name; @NonNull String password;

        Integer authCodeEncrypt() {
            return (name + authKey).hashCode();
        }
    }

    private static final TargetSet instance = new TargetSet();
    private final ResponseMessageFactory factory;
    static TargetSet getInstance(){ return instance; }

    private final Map<Integer, User> userMap;
    private final String authKey = "Catherine";

    private TargetSet() {
        factory = ResponseMessageFactory.getInstance();
        userMap = new HashMap<>();
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


    @Mapping(value = "/login", method = {WebMethods.GET})
    HttpResponseMessage login(HttpRequestMessage msg) {
        // todo: login system
        return factory.produce(500);
    }

    @Mapping(value = "/register", method = {WebMethods.POST})
    HttpResponseMessage register(HttpRequestMessage msg) {
        Map<String, String> argMap = parseArgs(msg.getTarget());
        if (argMap == null) return factory.produce(405);
        Log.debug("beep");
        HttpResponseMessage hrm = factory.produce(200);
        try {
            User user = new User(argMap.get("name"), argMap.get("password"));
            int authCode = user.authCodeEncrypt();
            userMap.put(authCode, user);
            hrm.addCookie("authCode", String.valueOf(authCode));
        } catch (Exception e) {
            e.printStackTrace();
            return factory.produce(405);
        }
        return hrm;
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
