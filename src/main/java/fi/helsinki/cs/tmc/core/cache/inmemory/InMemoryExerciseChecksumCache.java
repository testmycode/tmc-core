package fi.helsinki.cs.tmc.core.cache.inmemory;

import fi.helsinki.cs.tmc.core.cache.ExerciseChecksumCache;
import fi.helsinki.cs.tmc.core.domain.ExerciseIdentifier;

public class InMemoryExerciseChecksumCache extends InMemoryKeyValueCache<ExerciseIdentifier, String> implements ExerciseChecksumCache {
}
