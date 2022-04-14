package server.target;

import client.HttpRequestMessage;
import server.HttpResponseMessage;
import server.Mapping;
import util.Config;

public class Html extends TargetSet {
    @Mapping("/lab-m3")
    public static HttpResponseMessage labM3(HttpRequestMessage msg) {
        HttpResponseMessage hrm = factory.produce(200);
        hrm.setBodyAsFile("static_html/M3_ 系统调用 Profiler (sperf).html");
        return hrm;
    }

    @Mapping("/jyy-os")
    public static HttpResponseMessage jyyOs(HttpRequestMessage msg) {
        HttpResponseMessage hrm = factory.produce(200);
        hrm.setBodyAsFile("static_html/test_page_1.html");
        return hrm;
    }
}
