package fi.helsinki.cs.tmc.core.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Folders {

    static Path tmp = null;

    public static Path tempFolder() {
        if (tmp != null) {
            return tmp;
        }
        synchronized (Folders.class) {
            if (tmp == null) {
                try {
                    tmp = Files.createTempDirectory("tmc-core");
                }
                catch (IOException e) {
                    tmp = Paths.get(System.getProperty("java.io.tmpdir"));
                }
            }
            return tmp;
        }
    }
}
