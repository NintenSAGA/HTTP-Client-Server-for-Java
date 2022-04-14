package server.target;

import client.HttpRequestMessage;
import server.HttpResponseMessage;
import server.Mapping;
import util.Log;
import util.WebMethods;

public class Common extends TargetSet {
    @Mapping(value = "Missing", method = {WebMethods.GET, WebMethods.POST})
    static HttpResponseMessage responseOnMis(HttpRequestMessage msg) {
        Log.debug("Target not found!");
        // todo
        return factory.produce(404);
    }

    @Mapping(value = "/test", method = WebMethods.GET)
    static HttpResponseMessage test(HttpRequestMessage msg) {
        HttpResponseMessage hrm = factory.produce(200);
        hrm.setBodyAsPlainText("逐梦演艺圈");
        return hrm;
    }

    @Mapping(value = "/moved", method = WebMethods.GET)
    static HttpResponseMessage moved(HttpRequestMessage msg) {
        return factory.produce(301, "/test");
    }
}
