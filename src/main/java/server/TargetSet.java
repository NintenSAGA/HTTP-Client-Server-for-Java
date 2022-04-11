package server;

import client.HttpRequestMessage;
import util.Log;

class TargetSet {
    private static final TargetSet instance = new TargetSet();
    private final ResponseMessageFactory factory;
    private TargetSet() { factory = ResponseMessageFactory.getInstance(); }
    static TargetSet getInstance(){ return instance; }


    @Mapping("Missing")
    HttpResponseMessage responseOnMis(HttpRequestMessage msg) {
        Log.debug("Target not found!");
        // todo
        return factory.produce404();
    }

    @Mapping("/test")
    HttpResponseMessage test(HttpRequestMessage msg) {
        System.err.println("Hi you're calling this method");
        System.err.println(msg.flatMessage());
        return factory.produce200();
    }
}
