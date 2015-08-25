package fi.helsinki.cs.tmc.core.cache.filebased;

import fi.helsinki.cs.tmc.core.cache.ExerciseChecksumCache;
import fi.helsinki.cs.tmc.core.domain.ExerciseIdentifier;

import java.io.FileNotFoundException;
import java.nio.file.Path;

public class FileBasedExerciseChecksumCache extends FileBasedKeyValueCache<ExerciseIdentifier, String> implements ExerciseChecksumCache {

    public FileBasedExerciseChecksumCache(Path cacheFile) throws FileNotFoundException {
        super(cacheFile);
    }
}
