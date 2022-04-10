package server;

import java.util.Arrays;
import java.util.HashSet;

public class TargetHandler {
    static final HashSet<String> supportedMethods;
    static {
        supportedMethods = new HashSet<>();
        supportedMethods.addAll(Arrays.asList("GET POST".split(" ")));
    }

    /**
     * Handling the request based on the method and the target <br/>
     * @param method Request Method
     * @param target Request Target
     * @return Response object
     */
    public static HttpResponseMessage handle(String method, String target) {
        assert (supportedMethods.contains(method));
        // todo: Planned to be solved with Java reflection
        return null;
    }
}
