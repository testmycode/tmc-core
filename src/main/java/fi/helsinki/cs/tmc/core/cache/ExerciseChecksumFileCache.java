package fi.helsinki.cs.tmc.core.cache;

import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

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
    public void write(List<Exercise> exercises) throws TmcCoreException {
        Map<String, Map<String, String>> checksums = readCacheFile(cacheFile);

        for (Exercise exercise : exercises) {
            updateExerciseChecksum(checksums, exercise);
        }

        writeCache(checksums, cacheFile);
    }

    @Override
    public Map<String, Map<String, String>> read() throws TmcCoreException {
        Map<String, Map<String, String>> checksums = readCacheFile(cacheFile);

        if (checksums == null) {
            checksums = new HashMap<>();
        }

        return checksums;
    }

    @Override
    public void moveCache(Path newCache) throws TmcCoreException {
        Map<String, Map<String, String>> checksums = readCacheFile(cacheFile);
        writeCache(checksums, newCache);

        try {
            Files.delete(cacheFile);
        } catch (IOException ex) {
            throw new TmcCoreException("Unable to delete cache at " + cacheFile, ex);
        }

        this.cacheFile = newCache;
    }

    @Override
    public Path getCacheFile() {
        return this.cacheFile;
    }

    private void writeCache(Map<String, Map<String, String>> checksums, Path file) throws TmcCoreException {
        byte[] bytes = parser.toJson(checksums, Map.class).getBytes();
        try {
            Files.write(file, bytes, StandardOpenOption.WRITE);
        } catch (IOException ex) {
            throw new TmcCoreException("Unable to cache exercises: Can not write file", ex);
        }
    }

    private void updateExerciseChecksum(
            Map<String, Map<String, String>> checksums, Exercise exercise) {
        if (!checksums.containsKey(exercise.getCourseName())) {
            checksums.put(exercise.getCourseName(), new HashMap<String, String>());
        }
        checksums.get(exercise.getCourseName()).put(exercise.getName(), exercise.getChecksum());
    }

    private Map<String, Map<String, String>> readCacheFile(Path file) throws TmcCoreException {
        String oldCacheData = readCacheData(file);
        Map<String, Map<String, String>> checksums = new HashMap<>();
        if (oldCacheData != null && !oldCacheData.isEmpty()) {

            try {
                checksums = parser.fromJson(oldCacheData, TYPE_TOKEN);
            } catch (JsonSyntaxException ex) {
                System.err.println("WARNING: corrupt cache file, ignoring and overwriting");
                checksums = new HashMap<>();
            }
        }

        return checksums;
    }

    private String readCacheData(Path file) throws TmcCoreException {
        try {
            return new String(Files.readAllBytes(file), "UTF-8");
        } catch (IOException ex) {
            throw new TmcCoreException("Unable to cache exercises: Can not read file", ex);
        }
    }
}
