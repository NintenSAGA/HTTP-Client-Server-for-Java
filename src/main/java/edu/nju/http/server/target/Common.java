package edu.nju.http.server.target;

import edu.nju.http.message.HttpRequestMessage;
import edu.nju.http.message.HttpResponseMessage;
import edu.nju.http.util.Config;
import edu.nju.http.util.Log;
import edu.nju.http.message.consts.WebMethods;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class Common extends TargetSet {
    @Mapping(value = Config.MISSING, method = {WebMethods.GET, WebMethods.POST})
    public static HttpResponseMessage responseOnMis(HttpRequestMessage msg) {
        Log.debug("Target not found!");
        HttpResponseMessage hrm = factory.produce(404);
        hrm.setBodyAsPlainText("Page not found!");
        return hrm;
    }

    @Mapping(value = "/test", method = WebMethods.GET)
    public static HttpResponseMessage test(HttpRequestMessage msg) {
        HttpResponseMessage hrm = factory.produce(200);
        hrm.setBodyAsPlainText("You got the place!!!");
        return hrm;
    }

    @Mapping(value = "/moved", method = WebMethods.GET)
    public static HttpResponseMessage moved(HttpRequestMessage msg) {
        return factory.produce(301, "/test");
    }

    @Mapping(value = "/found", method = WebMethods.GET)
    public static HttpResponseMessage found(HttpRequestMessage msg) {
        return factory.produce(302, "/test");
    }

    @Mapping(value = "/panic", method = WebMethods.GET)
    public static HttpResponseMessage panic(HttpRequestMessage msg) throws IOException {
        throw new IOException();
    }
}
