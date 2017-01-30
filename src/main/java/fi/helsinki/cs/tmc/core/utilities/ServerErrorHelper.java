package fi.helsinki.cs.tmc.core.utilities;

import fi.helsinki.cs.tmc.core.exceptions.FailedHttpResponseException;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;

import com.google.common.annotations.Beta;

@Beta
public class ServerErrorHelper {

    /**
     * Used to humanize possible authentication error.
     */
    public static String getServerExceptionMsg(Throwable throwable) {
        if (throwable instanceof FailedHttpResponseException
                || throwable instanceof TmcCoreException) {
            if (throwable instanceof TmcCoreException
                    || ((FailedHttpResponseException)throwable).getStatusCode() == 401) {
                return "Check your username, password and server address in TMC -> Settings.";
            }
        }
        return throwable.getMessage();
    }
}
