package fi.helsinki.cs.tmc.core.cache;

import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface ExerciseChecksumCache {
    void write(List<Exercise> exercises) throws IOException;

    Map<String, Map<String, String>> read() throws IOException;

    void moveCache(Path newCache) throws IOException;

    Path getCacheFile();
}
