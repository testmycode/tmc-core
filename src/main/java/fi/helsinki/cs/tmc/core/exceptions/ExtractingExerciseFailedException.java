package fi.helsinki.cs.tmc.core.exceptions;

import fi.helsinki.cs.tmc.core.domain.Exercise;

public class ExtractingExerciseFailedException extends TmcCoreException {
    public ExtractingExerciseFailedException(Exercise exercise, Exception ex) {
        super("Extracting zip for " + exercise.getName() + " failed", ex);
    }
}
