package server;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
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

    private final ResponseMessageFactory factory;
    private final TargetSet targetSet;
    private final Set<String> supportedMethods;
    private final Map<String, Method> targetToMethod;

    private TargetHandler() {
        Log.debug("Target Handler initializing...");
        factory = ResponseMessageFactory.getInstance();
        targetSet = TargetSet.getInstance();

        supportedMethods = new HashSet<>();
        try {
            Class<?> methods = Class.forName("server.TargetSet$Methods");
            for (Field field : methods.getDeclaredFields())
                supportedMethods.add(field.getName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            assert false;
        }
        supportedMethods.addAll(Arrays.asList("GET POST".split(" ")));
        targetToMethod = new HashMap<>();

        Class<Mapping> mappingClass = Mapping.class;
        for (Method method : targetSet.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(mappingClass)) {
                assert checkMethod(method);
                Mapping mapping = method.getDeclaredAnnotation(mappingClass);
                targetToMethod.put(mapping.value(), method);
                Log.debug("[%s] %s -> %s".formatted(String.join(", ", mapping.method()), mapping.value(), method.getName()));
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
        if (!Log.testExpect("Parm Length", METHOD_PARAM.length, methodParams.length))
            return false;
        for (int i = 0; i < METHOD_PARAM.length; i++) {
            if (!Log.testExpect("Param %d".formatted(i), methodParams[i].getName(), METHOD_PARAM[i].getName()))
                return false;
        }
        if (!Log.testExpect("Return type", METHOD_RETURN.getName(), method.getReturnType().getName()))
            return false;
        Log.debug("Success!");
        return true;
    }

    public static TargetHandler getInstance() {
        return instance;
    }

    /**
     * Handling the request based on the method and the target
     * @return Response object
     */
    public HttpResponseMessage handle(HttpRequestMessage msg) {
        if (!supportedMethods.contains(msg.getMethod()))
            return factory.produce(405);

        Log.debug("Message received, target: " + msg.getTarget());

        try {
            String target = msg.getTarget();
            if (!targetToMethod.containsKey(target)) target = "Missing";
            Method method = targetToMethod.get(target);

            if (Arrays.binarySearch(method.getDeclaredAnnotation(Mapping.class).method(), msg.getMethod()) < 0)
                return factory.produce(405);

            return (HttpResponseMessage) targetToMethod.get(target).invoke(targetSet, msg);

        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return factory.produce(500);
        }
    }
}
