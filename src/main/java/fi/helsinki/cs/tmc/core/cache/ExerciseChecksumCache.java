package fi.helsinki.cs.tmc.core.cache;

import fi.helsinki.cs.tmc.core.domain.ExerciseIdentifier;

public interface ExerciseChecksumCache extends KeyValueCache<ExerciseIdentifier, String> {
}
