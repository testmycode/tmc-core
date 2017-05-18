package fi.helsinki.cs.tmc.core.exceptions;

import fi.helsinki.cs.tmc.core.domain.Exercise;
import java.net.URI;

public class ExtractingExericeFailedException extends TmcCoreException {
    public ExtractingExericeFailedException(Exercise exercise, Exception ex) {
        super("Extracting zip for " + exercise.getName() + " failed", ex);
    }
    
    // extract exercise when name is unknown, 
    public ExtractingExericeFailedException(URI url, Exception ex) {
        super("Extracting zip from " + url.toString() + " failed", ex);
    }
    
}
