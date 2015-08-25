package fi.helsinki.cs.tmc.core.cache.filebased;

import fi.helsinki.cs.tmc.core.cache.Cache;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileBasedCache<T> implements Cache<T> {

    private static final String ENCODING = "UTF-8";

    private final Type type_token;
    private Path cacheFile;
    private Gson parser;

    public FileBasedCache(Path cacheFile, Cache<T> oldCache) throws IOException {
        this(cacheFile);
        write(oldCache.read());
    }

    public FileBasedCache(Path cacheFile) throws FileNotFoundException {
        this.cacheFile = cacheFile;
        this.parser = new Gson();

        type_token = new TypeToken<T>() {}.getType();

        assertValidCacheLocation(cacheFile);
    }

    public Path getCacheFile() {
        return this.cacheFile;
    }

    @Override
    public void write(T data) throws IOException {
        write(data, this.cacheFile);
    }

    private void write(T data, Path target) throws IOException {
        Files.write(target, parser.toJson(data).getBytes());
    }

    @Override
    public T read() throws IOException {
        return read(this.cacheFile);
    }

    private T read(Path target) throws IOException {
        byte[] bytes = Files.readAllBytes(cacheFile);
        String json = new String(bytes, ENCODING);

        return parser.fromJson(json, type_token);
    }

    public void moveCache(Path newLocation) throws IOException {
        assertValidCacheLocation(newLocation);

        T cache = read(this.cacheFile);
        write(cache, newLocation);

        Path oldCache = this.cacheFile;
        this.cacheFile = newLocation;

        Files.delete(oldCache);
    }

    @Override
    public void clear() throws IOException {
        Files.write(this.cacheFile, new byte[]{});
    }

    private void assertValidCacheLocation(Path location) throws FileNotFoundException {
        if (Files.exists(location)) {
            if (!Files.isRegularFile(location)) {
                throw new FileNotFoundException("Invalid cache file: Not a normal file");
            }
            if (!Files.isReadable(location)) {
                throw new FileNotFoundException("Invalid cache file: Not readable");
            }
            if (!Files.isWritable(location)) {
                throw new FileNotFoundException("Invalid cache file: Not writable");
            }
        }
    }
}
