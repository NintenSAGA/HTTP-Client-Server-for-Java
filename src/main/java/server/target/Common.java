package server.target;

import message.HttpRequestMessage;
import message.HttpResponseMessage;
import server.Mapping;
import util.Log;
import message.consts.WebMethods;

public class Common extends TargetSet {
    @Mapping(value = "Missing", method = {WebMethods.GET, WebMethods.POST})
    public static HttpResponseMessage responseOnMis(HttpRequestMessage msg) {
        Log.debug("Target not found!");
        // todo: Missing
        return factory.produce(404);
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
}
