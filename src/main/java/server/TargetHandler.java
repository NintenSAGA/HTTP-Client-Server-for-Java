package server;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import message.HttpRequestMessage;
import message.HttpResponseMessage;
import org.json.JSONObject;
import util.Config;
import message.HttpMessage;
import util.Log;
import message.MessageHelper;
import message.consts.Headers;
import message.consts.WebMethods;

/**
 * A singleton handler
 */
public class TargetHandler {
    private static final TargetHandler instance = new TargetHandler();

    private final ResponseMessageFactory factory;
    private final Set<String> supportedMethods;
    private final Map<String, Method> targetToMethod;

    private TargetHandler() {
        Log.debug("Target Handler initializing...");
        factory = ResponseMessageFactory.getInstance();

        supportedMethods = new HashSet<>();
        try {
            Class<?> methods = Class.forName("message.consts.WebMethods");
            for (Field field : methods.getDeclaredFields())
                supportedMethods.add(field.getName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            assert false;
        }
        targetToMethod = new HashMap<>();

        JSONObject json = Config.getConfigAsJsonObj(Config.TARGET_PATH);
        Class<Mapping> mappingClass = Mapping.class;

        try {
            for (String prefix : json.keySet()) {
                for (Object className : json.getJSONArray(prefix).toList()) {
                    Class<?> targetClass = Class.forName("%s.%s".formatted(prefix, className));
                    for (Method method : targetClass.getDeclaredMethods()) {
                        if (method.isAnnotationPresent(mappingClass)) {
                            assert checkMethod(method);
                            Mapping mapping = method.getDeclaredAnnotation(mappingClass);
                            targetToMethod.put(mapping.value(), method);
                            Log.debug("[%s] %s -> %s".formatted(String.join(", ", mapping.method()), mapping.value(), method.getName()));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        if (!msg.getHttpVersion().equals(HttpMessage.HTTP11))
            return factory.produce(505);

        try {
            String target = msg.getTarget().split("\\?")[0];
            target = target.toLowerCase(Locale.ROOT);

            if (!targetToMethod.containsKey(target)) {
                // -------------------- 1. Static Resource -------------------- //
                Path path = getResourcePath(target);

                Log.debug("Search resource in path: ", path);

                if (WebMethods.GET.equals(msg.getMethod())
                    && Files.exists(path)
                ) {
                    return loadStaticResource(path, msg);
                } else {
                    target = "Missing";
                }
            }

            // -------------------- 2. Matched Target -------------------- //

            /*               Check method validity               */
            Method method = targetToMethod.get(target);
            if (Arrays.binarySearch(method.getDeclaredAnnotation(Mapping.class).method(), msg.getMethod()) < 0)
                return factory.produce(405);


            return (HttpResponseMessage) targetToMethod.get(target).invoke(null, msg);

        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return factory.produce(500);
        }
    }

    private Path getResourcePath(String target) {
        if (target.endsWith("/"))
            target += "index.html";
        return Path.of(Config.STATIC_DIR, target);
    }

    private HttpResponseMessage loadStaticResource(Path path, HttpRequestMessage request) {
        Log.debug("Resource found");

        if (request.containsHeader(Headers.IF_MODIFIED_SINCE)) {
            String time = request.getHeaderVal(Headers.IF_MODIFIED_SINCE);
            Date date = MessageHelper.parseTime(time);
            assert date != null;
            Date myDate = Config.getResourceLastModifiedTimeAsDate(path);
            if (myDate.compareTo(date) < 0) {
                return factory.produce(304);
            }
        }

        HttpResponseMessage hrm = factory.produce(200);
        hrm.setBodyAsFileWithAbsPath(path);
        return hrm;
    }
}
