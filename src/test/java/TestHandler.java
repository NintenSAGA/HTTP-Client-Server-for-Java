import client.HttpRequestMessage;
import org.junit.jupiter.api.Test;
import server.TargetHandler;

public class TestHandler {

    @Test
    public void checkTarget() {
        TargetHandler targetHandler = TargetHandler.getInstance();
        System.out.println("Response: " + targetHandler.handle(new HttpRequestMessage("GET", "/test")));;
        System.out.println("Response: " + targetHandler.handle(new HttpRequestMessage("POST", "/test")));;
        System.out.println("Response: " + targetHandler.handle(new HttpRequestMessage("POST", "/utadahikaru")));;
    }
}
