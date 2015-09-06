package fi.helsinki.cs.tmc.core.testhelpers;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Random;

public class FileWriterHelper {

    FileWriter writer;

    /**
     * Writes stuff to file.
     * @param path to write
     */
    public void writeStuffToFile(Path path) throws IOException {
        try {
            writer = new FileWriter(new File(path.toString()));
            Random random = new Random();
            for (int i = 0; i < 20; i++) {
                writer.append(random.nextBoolean() + "\n");
            }
        } catch (IOException ex) {
            fail("writer failed to init");
        }
        writer.close();
    }
}
