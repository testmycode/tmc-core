package fi.helsinki.cs.tmc.core.exceptions;

import fi.helsinki.cs.tmc.core.domain.Exercise;

public class ExerciseDownloadFailedException extends TmcCoreException {

    public ExerciseDownloadFailedException(Exercise exercise, Exception ex) {
        super("Downloading exercise " + exercise.getName() + " failed", ex);
    }
}
