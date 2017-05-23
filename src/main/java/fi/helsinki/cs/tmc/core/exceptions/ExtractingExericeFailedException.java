package fi.helsinki.cs.tmc.core.exceptions;

import fi.helsinki.cs.tmc.core.domain.Exercise;

public class ExtractingExericeFailedException extends TmcCoreException {
    public ExtractingExericeFailedException(Exercise exercise, Exception ex) {
        super("Extracting zip for " + exercise.getName() + " failed", ex);
    }
}
