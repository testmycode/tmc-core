package fi.helsinki.cs.tmc.core.cache;

import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Created by ljleppan on 18.8.2015.
 */
public interface ExerciseChecksumCache {
    void write(List<Exercise> exercises) throws TmcCoreException;

    Map<String, Map<String, String>> read() throws TmcCoreException;

    void moveCache(Path newCache) throws TmcCoreException;

    Path getCacheFile();
}
