package util;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.File;
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

    private static Path getPath(String resource) {
        URL url = ClassLoader.getSystemClassLoader().getResource(resource);
        assert url != null: "Wrong path!";
        try {
            return Path.of(url.toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
            assert false;
            return null;
        }
    }

    public static long getSizeOfResource(String resource) {
        Path path = getPath(resource);
        assert path != null;
        try {
            return Files.size(path);
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static byte[] getResourceAsByteArray(String resource) {
        Path path = getPath(resource);

        try {
            assert path != null;
            return Files.readAllBytes(path);
        } catch (IOException e) {
            e.printStackTrace();
            assert false;
            return new byte[]{ 0 };
        }
    }

    public static String getResourceAsString(String resource) {
        Path path = getPath(resource);

        try {
            assert path != null;
            return Files.readString(path);
        } catch (IOException e) {
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
