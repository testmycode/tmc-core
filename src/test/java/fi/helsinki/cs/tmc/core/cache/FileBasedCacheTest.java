package fi.helsinki.cs.tmc.core.cache;

import static org.junit.Assert.assertEquals;

import fi.helsinki.cs.tmc.core.cache.filebased.FileBasedCache;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileBasedCacheTest {

    private FileBasedCache<String> cache;
    private Path cacheLocation;

    @Before
    public void setUp() throws IOException {
        cacheLocation = Files.createTempFile("tmc-test-cache", ".cache");
        cache = new FileBasedCache<>(cacheLocation);
    }

    @After
    public void tearDown() throws IOException {
        Files.delete(cacheLocation);
    }

    @Test
    public void getCacheFileReturnsCorrectFile() {
        assertEquals(cache.getCacheFile(), cacheLocation);
    }

    @Test
    public void writeWritesData() throws IOException {
        cache.write("test");

        String contents = new String(Files.readAllBytes(cacheLocation));
        assertEquals("\"test\"", contents);
    }

    @Test
    public void writeOverwritesInsteadOfAppending() throws IOException {
        Files.write(cacheLocation, "\"old\"".getBytes());

        cache.write("new");

        String contents = new String(Files.readAllBytes(cacheLocation));
        assertEquals("\"new\"", contents);
    }

    @Test
    public void readReturnsCorrectData() throws IOException {
        Files.write(cacheLocation, "\"test\"".getBytes());

        assertEquals("test", cache.read());
    }

    @Test
    public void constructingFromExistingMovesData() throws IOException {
        cache.write("old");
        Path newLocation = Files.createTempFile("newcache", ".cache");
        FileBasedCache<String> newCache = new FileBasedCache<>(newLocation, cache);

        String contents = new String(Files.readAllBytes(newLocation));
        assertEquals("\"old\"", contents);
    }

    @Test
    public void moveCacheMovesCache() throws IOException {
        cache.write("old");
        Path newLocation = Files.createTempFile("newcache", ".cache");
        cache.moveCache(newLocation);

        String contents = new String(Files.readAllBytes(newLocation));
        assertEquals("\"old\"", contents);
    }

    @Test
    public void


}
