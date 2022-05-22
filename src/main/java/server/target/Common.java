package server.target;

import client.HttpRequestMessage;
import server.HttpResponseMessage;
import server.Mapping;
import util.Config;
import util.Log;
import util.consts.WebMethods;

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

    @Mapping(value = "/long_test", method = WebMethods.GET)
    public static HttpResponseMessage long_test(HttpRequestMessage msg) {
        HttpResponseMessage hrm = factory.produce(200);
        hrm.setBodyAsPlainText(Config.getResourceAsString("test_files/long_text.txt"));
        return hrm;
    }

    @Mapping(value = "/moved", method = WebMethods.GET)
    public static HttpResponseMessage moved(HttpRequestMessage msg) {
        return factory.produce(301, "/test");
    }
}
