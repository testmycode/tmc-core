package fi.helsinki.cs.tmc.core.exceptions;

public class AuthenticationFailedException extends TmcCoreException {

    public AuthenticationFailedException(Exception ex) {
        super("Authentication failed!", ex);
    }
}
