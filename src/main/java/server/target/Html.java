package server.target;

import client.HttpRequestMessage;
import server.HttpResponseMessage;
import server.Mapping;
import util.Config;

public class Html extends TargetSet {

    @Mapping("/jyy-os")
    public static HttpResponseMessage jyyOs(HttpRequestMessage msg) {
        HttpResponseMessage hrm = factory.produce(200);
        hrm.setBodyAsFile("static_html/test_page_1.html");
        return hrm;
    }
}
