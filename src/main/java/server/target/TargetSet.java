package server.target;

import client.HttpRequestMessage;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import server.HttpResponseMessage;
import server.Mapping;
import server.ResponseMessageFactory;
import util.Log;
import util.MessageHelper;
import util.WebMethods;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;


public abstract class TargetSet {
    protected static final ResponseMessageFactory factory;

    static {
        factory = ResponseMessageFactory.getInstance();
    }

    protected static Map<String, String> parseArgs(String target) {
        int st = 0;
        while ( st < target.length() && target.charAt(st) != '?' ) st++;
        if (st == target.length()) return null;

        String argStr = target.substring(st + 1);
        try {
            return Arrays.stream(argStr.split("\\&"))
                            .map(s -> s.split("="))
                            .collect(Collectors.toMap(a -> a[0], a->a[1]));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
