package hy.tmc.core.exceptions;

public class ExpiredException extends Exception {
    /**
     * ExpiredException is thrown when user tries to submit or download expired exercise.
     */
    public ExpiredException() {
        super();
    }

    /**
     * ExpiredException can give a message.
     * @param message is a final string.
     */
    public ExpiredException(final String message) {
        super(message);
    }

    /**
     * ExpiredException can have cause as parameter.
     *
     * @param message final string message
     * @param cause cause why exception is thrown
     */
    public ExpiredException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * ExpiredException can only have a cause.
     *
     * @param cause why exception is thrown
     */
    public ExpiredException(final Throwable cause) {
        super(cause);
    }
}
