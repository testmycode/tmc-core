package fi.helsinki.cs.tmc.core.persistance;

import fi.helsinki.cs.tmc.core.holders.TmcSettingsHolder;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigFile {

    private final String name;
    private Path file;

    public ConfigFile(String name) {
        this.name = name;
    }

    public Path getFile() throws IOException {
        if (file == null) {
            Path root = TmcSettingsHolder.get().getConfigRoot();
            this.file = root.resolve(name);
            if (!Files.exists(this.file)) {
                Files.createFile(this.file);
            }
        }
        return file;
    }

    public Writer getWriter() throws IOException {
        return Files.newBufferedWriter(getFile(), Charset.forName("UTF-8"));
    }

    public Reader getReader() throws IOException {
        return Files.newBufferedReader(getFile(), Charset.forName("UTF-8"));
    }

    public void writeContents(String content) throws IOException {
        Writer writer = getWriter();
        try {
            writer.write(content);
        } finally {
            writer.close();
        }
    }

    public String readContents() throws IOException {
        return new String(Files.readAllBytes(getFile()));
    }

    public boolean exists() {
        return true;
        //TODO: getFile always creates the file. This is not optimal. Refactor this.
    }
}
