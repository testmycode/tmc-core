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
        return getResource(clazz, "/__files/" + location);
    }

    public static Path getZip(Class<?> clazz, String filename) {
        return getResource(clazz, "/__files/" + filename);
    }

    private static Path getResource(Class<?> clazz, String filename) {
        try {
            URL url = clazz.getResource(filename);

            if (url != null) {
                return Paths.get(url.toURI());
            }

            return null;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
