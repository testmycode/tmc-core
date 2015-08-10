
package fi.helsinki.cs.tmc.core.exceptions;

import java.io.IOException;

/**
 * TmcServerException is thrown, if there occurs exception in TmcServer.
 */
public class TmcServerException extends IOException {

    public TmcServerException() {
        super();
    }

    public TmcServerException(final String message) {
        super(message);
    }

    public TmcServerException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public TmcServerException(final Throwable cause) {
        super(cause);
    }
}
