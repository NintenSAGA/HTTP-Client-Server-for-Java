package util;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Config {
    public final static String GLOBAL_HEADERS = "global_headers.json";
    public final static String DEFAULT_STATUS_TEXT = "default_status_text.json";

    public static InputStream getConfigAsStream(String config) {
        InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream(config);
        assert is != null: "Config [%s] not found!".formatted(config);
        return is;
    }

    public static JSONObject getConfigAsJsonObj(String config) {
        return new JSONObject(
                new JSONTokener(
                        new BufferedReader(
                                new InputStreamReader(
                                        getConfigAsStream(config)
                                )
                        )
                )
        );
    }
}
