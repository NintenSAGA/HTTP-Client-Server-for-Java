import client.HttpRequestMessage;
import org.junit.jupiter.api.Test;
import server.TargetHandler;

public class HandlerTests {

    @Test
    public void checkTarget() {
        TargetHandler targetHandler = TargetHandler.getInstance();
        System.out.println("Response: " + targetHandler.handle(new HttpRequestMessage("GET", "/test")));;
        System.out.println("Response: " + targetHandler.handle(new HttpRequestMessage("POST", "/test")));;
        System.out.println("Response: " + targetHandler.handle(new HttpRequestMessage("POST", "/utadahikaru")));;
        System.out.println("Response: " + targetHandler.handle(new HttpRequestMessage("GET", "/moved")));;
        System.out.println("Response: " + targetHandler.handle(new HttpRequestMessage("GET", "/test?user=fff")));;
    }
}
