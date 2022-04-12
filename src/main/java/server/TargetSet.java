package server;

import client.HttpRequestMessage;
import util.Log;

/**
 * Container of target mapping methods
 */
class TargetSet {
    static class Methods {
        private static final String GET = "GET";
        private static final String POST = "POST";
    }

    private static final TargetSet instance = new TargetSet();
    private final ResponseMessageFactory factory;
    private TargetSet() { factory = ResponseMessageFactory.getInstance(); }
    static TargetSet getInstance(){ return instance; }


    @Mapping(value = "Missing", method = {Methods.GET, Methods.POST})
    HttpResponseMessage responseOnMis(HttpRequestMessage msg) {
        Log.debug("Target not found!");
        // todo
        return factory.produce(404);
    }

    @Mapping(value = "/test", method = Methods.GET)
    HttpResponseMessage test(HttpRequestMessage msg) {
        System.err.println("Hi you're calling this method");
        System.err.println(msg.flatMessage());
        HttpResponseMessage hrm = factory.produce(200);
        hrm.setBody("逐梦演艺圈");
        return hrm;
    }
}
