package fi.helsinki.cs.tmc.core.exceptions;

public class UninitializedHolderException extends RuntimeException {

    public UninitializedHolderException() {
        super("Attempted to get item from a holder that was not properly initialized");
    }
}
