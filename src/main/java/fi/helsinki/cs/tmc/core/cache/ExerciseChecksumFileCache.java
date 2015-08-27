package fi.helsinki.cs.tmc.core.cache;

import fi.helsinki.cs.tmc.core.domain.Exercise;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExerciseChecksumFileCache implements ExerciseChecksumCache {

    private static final Logger log = LoggerFactory.getLogger(ExerciseChecksumFileCache.class);

    private static Type TYPE_TOKEN = new TypeToken<Map<String, Map<String, String>>>() {}.getType();

    private Gson parser;
    private Path cacheFile;

    public ExerciseChecksumFileCache(Path cacheFile) throws FileNotFoundException {
        this.cacheFile = cacheFile;
        this.parser = new Gson();

        if (Files.notExists(cacheFile)) {
            throw new FileNotFoundException("Cache file " + cacheFile + "does not exist");
        }
    }

    @Override
    public void write(List<Exercise> exercises) throws IOException {
        Map<String, Map<String, String>> checksums = readCacheFile(cacheFile);

        for (Exercise exercise : exercises) {
            updateExerciseChecksum(checksums, exercise);
        }

        writeCache(checksums, cacheFile);
    }

    @Override
    public Map<String, Map<String, String>> read() throws IOException {
        Map<String, Map<String, String>> checksums = readCacheFile(cacheFile);

        if (checksums == null) {
            checksums = new HashMap<>();
        }

        return checksums;
    }

    @Override
    public void moveCache(Path newCache) throws IOException {
        Map<String, Map<String, String>> checksums = readCacheFile(cacheFile);
        writeCache(checksums, newCache);

        Files.delete(cacheFile);

        this.cacheFile = newCache;
    }

    @Override
    public Path getCacheFile() {
        return this.cacheFile;
    }

    private void writeCache(Map<String, Map<String, String>> checksums, Path file)
            throws IOException {
        byte[] bytes = parser.toJson(checksums, Map.class).getBytes();
        Files.write(file, bytes, StandardOpenOption.WRITE);
    }

    private void updateExerciseChecksum(
            Map<String, Map<String, String>> checksums, Exercise exercise) {
        if (!checksums.containsKey(exercise.getCourseName())) {
            checksums.put(exercise.getCourseName(), new HashMap<String, String>());
        }
        checksums.get(exercise.getCourseName()).put(exercise.getName(), exercise.getChecksum());
    }

    private Map<String, Map<String, String>> readCacheFile(Path file) throws IOException {
        String oldCacheData = readCacheData(file);
        Map<String, Map<String, String>> checksums = new HashMap<>();
        if (oldCacheData != null && !oldCacheData.isEmpty()) {

            try {
                checksums = parser.fromJson(oldCacheData, TYPE_TOKEN);
            } catch (JsonSyntaxException ex) {
                log.warn("WARNING: corrupt cache file, ignoring and overwriting");
                checksums = new HashMap<>();
            }
        }

        return checksums;
    }

    private String readCacheData(Path file) throws IOException {
        return new String(Files.readAllBytes(file), "UTF-8");
    }
}
