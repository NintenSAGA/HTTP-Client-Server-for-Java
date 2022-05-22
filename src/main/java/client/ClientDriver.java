package client;

import util.consts.WebMethods;

import java.io.IOException;

public class ClientDriver {
    public static void main(String[] args) {
        String hostName = "www.baidu.com";
        int hostPort = 80;
        boolean longConnection = false;

        String method = WebMethods.GET;
        String target = "/";
        String param = null;
        String body = null;

        try {
            HttpClient client = new HttpClient(hostName, hostPort, longConnection);
            var hrm = client.request(method, target, param, body);

            System.out.println(
                    HttpClient.present(hrm)
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
