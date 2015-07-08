
package hy.tmc.core.communication;

import hy.tmc.core.exceptions.TmcServerException;

/**
 * Validator throws error that will be throwed to user via TmcCore interface.
 * http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html
 */
public class HttpStatusValidator {
    
    private final String unauthorized_401 = "Unauthorized error on server. "
                    + "Please check that the username, password and server"
                    + " address are correct. You may have these credentials on"
                    + " an another server?";
    private final String internalserver_500 = "Error occured on TMC-server: statuscode ";
    
    public void validate(int statuscode) throws TmcServerException {
        if (statuscode == 401) {
            throw new TmcServerException(unauthorized_401);
        }
        if (statuscode >= 500 && statuscode < 600) {
            throw new TmcServerException(internalserver_500 + statuscode);
        }
    }
}
