package fi.helsinki.cs.tmc.core.utils;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class TestUtils {

    /**
     * Returns a path to a resource residing in the ResourceDir of the given
     * class.
     */
    public static Path getProject(Class<?> clazz, String location) {
        try {
            URL url = clazz.getResource("/__files/" + location);

            if (url != null) {
                return Paths.get(url.toURI());
            }

            return null;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static Path getZip(Class<?> clazz, String filename) {
        try {
            URL url = clazz.getResource("/__files/" + filename);

            if (url != null) {
                return Paths.get(url.toURI());
            }

            return null;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
