import client.HttpRequestMessage;
import org.junit.jupiter.api.Test;
import server.HttpResponseMessage;

class TestDrive {
    public static void main(String[] args) {
        System.out.println("Hello");
    }

    @Test
    public void sample() {
        HttpRequestMessage hrm = new HttpRequestMessage("GET", "/");
        System.out.println(hrm.flatMessage());
    }

}