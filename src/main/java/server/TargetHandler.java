package server;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import client.HttpRequestMessage;
import util.Log;

/**
 * A singleton handler
 */
public class TargetHandler {
    private static final TargetHandler instance = new TargetHandler();

    final Set<String> supportedMethods;
    final Map<String, Method> targetToMethod;

    private TargetHandler() {
        Log.debug("Target Handler initializing...");
        supportedMethods = new HashSet<>();
        supportedMethods.addAll(Arrays.asList("GET POST".split(" ")));
        targetToMethod = new HashMap<>();

        Class<Mapping> mappingClass = Mapping.class;
        for (Method method : this.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(mappingClass)) {
                assert checkMethod(method);
                targetToMethod.put(method.getDeclaredAnnotation(mappingClass).target(), method);
                Log.debug("%s -> %s".formatted(method.getDeclaredAnnotation(mappingClass).target(), method.getName()));
            }
        }
    }

    /**
     * Check the legality of the method. <br/>
     * Only invoked when assertion is enabled
     */
    private boolean checkMethod(Method method) {
        Class<?>[] METHOD_PARAM = new Class<?>[]{ HttpRequestMessage.class };
        Class<?> METHOD_RETURN = HttpResponseMessage.class;

        Log.debug("Now checking method [" + method.getName() + "]");
        Class<?>[] methodParams = method.getParameterTypes();
        if (methodParams.length != METHOD_PARAM.length) {
            Log.testExpect("Param length", "" + METHOD_PARAM.length, "" + methodParams.length);
            return false;
        }
        for (int i = 0; i < METHOD_PARAM.length; i++) {
            if (!methodParams[i].equals(METHOD_PARAM[i])) {
                Log.testExpect("Param %d".formatted(i), methodParams[i].getName(), METHOD_PARAM[i].getName());
                return false;
            }
        }
        if (!method.getReturnType().equals(METHOD_RETURN)) {
            Log.testExpect("Return type", METHOD_RETURN.getName(), method.getReturnType().getName());
            return false;
        }
        Log.debug("Success!");
        return true;
    }

    public static TargetHandler get() {
        return instance;
    }

    /**
     * Handling the request based on the method and the target
     * @return Response object
     */
    public HttpResponseMessage handle(HttpRequestMessage msg) {
        assert (supportedMethods.contains(msg.getMethod()));
        Log.debug("Message received, target: " + msg.getTarget());
        try {
            String target = msg.getTarget();
            if (!targetToMethod.containsKey(target)) target = "Missing";
            return (HttpResponseMessage) targetToMethod.get(target).invoke(this, msg);

        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Mapping(target = "Missing")
    private HttpResponseMessage responseOnMis(HttpRequestMessage msg) {
        Log.debug("Target not found!");
        // todo
        return null;
    }

    @Mapping(target = "/test")
    private HttpResponseMessage test(HttpRequestMessage msg) {
        System.err.println("Hi you're calling this method");
        System.err.println(msg.flatMessage());
        return null;
    }
}
