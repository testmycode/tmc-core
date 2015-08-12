package fi.helsinki.cs.tmc.core.exceptions;

/**
 * TmcCoreException is thrown when something goes wrong with protocol rules.
 */
public class TmcCoreException extends Exception {

    /**
     * TmcCoreException is thrown when system gets invalid protocol.
     */
    public TmcCoreException() {
        super();
    }

    /**
     * TmcCoreException can give a message.
     * @param message is a final string.
     */
    public TmcCoreException(final String message) {
        super(message);
    }

    /**
     * TmcCoreException can have cause as parameter.
     *
     * @param message final string message
     * @param cause cause why exception is thrown
     */
    public TmcCoreException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * TmcCoreException can only have a cause.
     *
     * @param cause why exception is thrown
     */
    public TmcCoreException(final Throwable cause) {
        super(cause);
    }
}
