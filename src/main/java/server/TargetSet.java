package server;

import client.HttpRequestMessage;
import util.Log;
import util.WebMethods;

/**
 * Singleton container of target mapping methods
 */
class TargetSet {
    private static final TargetSet instance = new TargetSet();
    private final ResponseMessageFactory factory;
    private TargetSet() { factory = ResponseMessageFactory.getInstance(); }
    static TargetSet getInstance(){ return instance; }

    @Mapping(value = "/login", method = {WebMethods.GET})
    HttpResponseMessage login(HttpRequestMessage msg) {
        // todo: login system
        return factory.produce(500);
    }

    @Mapping(value = "/register", method = {WebMethods.POST})
    HttpResponseMessage register(HttpRequestMessage msg) {
        // todo: login system
        return factory.produce(500);
    }


    @Mapping(value = "Missing", method = {WebMethods.GET, WebMethods.POST})
    HttpResponseMessage responseOnMis(HttpRequestMessage msg) {
        Log.debug("Target not found!");
        // todo
        return factory.produce(404);
    }

    @Mapping(value = "/test", method = WebMethods.GET)
    HttpResponseMessage test(HttpRequestMessage msg) {
        System.err.println("Hi you're calling this method");
        System.err.println(msg.flatMessage());
        HttpResponseMessage hrm = factory.produce(200);
        hrm.setBodyAsPlainText("逐梦演艺圈");
        return hrm;
    }

    @Mapping(value = "/moved", method = WebMethods.GET)
    HttpResponseMessage moved(HttpRequestMessage msg) {
        return factory.produce(301, "www.baidu.com");
    }
}
