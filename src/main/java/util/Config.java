package util;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class Config {
    public final static String GLOBAL_HEADERS       = "global_headers.json";
    public final static String DEFAULT_STATUS_TEXT  = "default_status_text.json";
    public final static String TARGET_PATH          = "target_path.json";
    public final static String MIME                 = "mime.json";

    public static byte[] getResourceAsByteArray(String resource) {
        URL url = ClassLoader.getSystemClassLoader().getResource(resource);
        assert url != null: "Wrong path!";
        try {
            Path path = Path.of(url.toURI());
            return Files.readAllBytes(path);
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
            assert false;
            return new byte[]{ 0 };
        }
    }

    public static String getResourceAsString(String resource) {
        URL url = ClassLoader.getSystemClassLoader().getResource(resource);
        assert url != null: "Wrong path!";
        try {
            Path path = Path.of(url.toURI());
            return Files.readString(path);
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
            assert false;
            return "null";
        }
    }

    public static InputStream getResourceAsStream(String resource) {
        InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream(resource);
        assert is != null: "Config [%s] not found!".formatted(resource);
        return is;
    }

    public static JSONObject getConfigAsJsonObj(String config) {
        return new JSONObject(
                new JSONTokener(
                        new BufferedReader(
                                new InputStreamReader(
                                        getResourceAsStream(config)
                                )
                        )
                )
        );
    }
}
