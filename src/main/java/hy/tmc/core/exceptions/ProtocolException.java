package hy.tmc.core.exceptions;

/**
 * ProtocolException is thrown when something goes wrong with protocol rules.
 */
public class ProtocolException extends Exception {

    /**
     * ProtocolException is thrown when system gets invalid protocol.
     */
    public ProtocolException() {
        super();
    }

    /**
     * ProtocolException can give a message.
     * @param message is a final string.
     */
    public ProtocolException(final String message) {
        super(message);
    }

    /**
     * ProtocolException can have cause as parameter.
     * 
     * @param message final string message
     * @param cause cause why exception is thrown
     */
    public ProtocolException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * ProtocolException can only have a cause.
     * 
     * @param cause why exception is thrown
     */
    public ProtocolException(final Throwable cause) {
        super(cause);
    }
}
