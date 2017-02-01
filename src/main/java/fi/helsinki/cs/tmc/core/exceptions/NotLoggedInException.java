package fi.helsinki.cs.tmc.core.exceptions;

public class NotLoggedInException extends TmcCoreException {

    public NotLoggedInException() {
        super("Not logged in!");
    }
}
