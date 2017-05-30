package fi.helsinki.cs.tmc.core.exceptions;

import fi.helsinki.cs.tmc.core.domain.Exercise;

import java.net.URI;

public class ExtractingExerciseFailedException extends TmcCoreException {
    public ExtractingExerciseFailedException(final Exercise exercise, final Exception ex) {
        super("Extracting zip for " + exercise.getName() + " failed", ex);
    }

    public ExtractingExerciseFailedException(final URI url, final Exception ex) {
        super("Extracting zip from " + url.toString() + " failed", ex);
    }
}
