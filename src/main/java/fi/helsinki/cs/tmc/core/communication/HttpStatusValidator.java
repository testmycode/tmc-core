package fi.helsinki.cs.tmc.core.communication;

import fi.helsinki.cs.tmc.core.exceptions.TmcServerException;

/**
 * Validator throws error that will be throwed to user via TmcCore interface.
 * http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html
 */
// TODO: relocate?
// TODO: followup somewhere: just start up authing(tunnarit tai oauth) and redo?
public class HttpStatusValidator {

    private static final String UNAUTHORIZED_401 =
            "Unauthorized error on server. "
                    + "Please check that the username, password and server"
                    + " address are correct. You may have these credentials on"
                    + " an another server?";
    private static final String INTERNALSERVER_500 = "Error occured on TMC-server: statuscode ";

    public void validate(int statuscode) throws TmcServerException {
        if (statuscode == 401) { // 403
            throw new TmcServerException(UNAUTHORIZED_401);
        }
        if (statuscode >= 500 && statuscode < 600) {
            throw new TmcServerException(INTERNALSERVER_500 + statuscode);
        }
    }
}
