package util;

import message.MessageHelper;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Date;

import static message.consts.Headers.CHUNKED;

public class Config {
    public final static String GLOBAL_HEADERS       = "global_headers.json";
    public final static String DEFAULT_STATUS_TEXT  = "default_status_text.json";
    public final static String TARGET_PATH          = "target_path.json";
    public final static String MIME                 = "mime.json";

    public static final String DATA_DIR;
    static {
        DATA_DIR = System.getProperty("user.dir") + "/Data";
    }
    public static final String CLIENT_PATH  = DATA_DIR + "/Client";
    public static final String SERVER_PATH  = DATA_DIR + "/Server";
    public static final String TEST_PATH    = DATA_DIR + "/Test";

    public final static String STATIC_DIR           = SERVER_PATH   + "/Static";
    public final static String CLIENT_CACHE         = CLIENT_PATH   + "/Cache";
    public final static String SERVER_CACHE         = SERVER_PATH   + "/Cache";
    public final static String TEST_CACHE           = TEST_PATH     + "/Cache";

    public static final String DOWNLOAD_PATH        = DATA_DIR      + "/Download";

    public final static int GZIP_THRESHOLD          = (1 << 20); // 1MB

    public final static String[] TRANSFER_ENCODINGS = { CHUNKED };

    public final static int SOCKET_BUFFER_SIZE = 1 << 10;

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
        return getSizeOfResource(path);
    }

    public static long getSizeOfResource(Path path) {
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

    public static Path getClientCacheDir() {
        return getPath(Config.CLIENT_CACHE);
    }

    public static String getResourceLastModifiedTime(String resource) {
        FileTime ft = Config.getResourceFileTime(resource);
        if (ft != null)
            return MessageHelper.getTime(new Date(ft.toMillis()));
        else
            return "Error";
    }

    public static Date getResourceLastModifiedTimeAsDate(Path path) {
        FileTime ft = Config.getResourceFileTime(path);
        assert ft != null;
        return new Date(ft.toMillis());
    }

    public static String getResourceLastModifiedTime(Path path) {
        return MessageHelper.getTime(
                getResourceLastModifiedTimeAsDate(path)
        );
    }

    public static FileTime getResourceFileTime(String resource) {
        Path path = getPath(resource);
        assert path != null;
        return getResourceFileTime(path);
    }

    public static FileTime getResourceFileTime(Path path) {
        try {
            return Files.getLastModifiedTime(path);
        } catch (IOException e) {
            return null;
        }
    }
}
