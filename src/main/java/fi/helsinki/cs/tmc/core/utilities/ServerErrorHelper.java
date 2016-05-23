package fi.helsinki.cs.tmc.core.utilities;

import fi.helsinki.cs.tmc.core.exceptions.FailedHttpResponseException;

import com.google.common.annotations.Beta;

@Beta
public class ServerErrorHelper {

    /**
     * Used to humanize possible authentication error.
     */
    public static String getServerExceptionMsg(Throwable throwable) {
        if (throwable instanceof FailedHttpResponseException) {
            if (((FailedHttpResponseException)throwable).getStatusCode() == 401) {
                return "Check your username and password in TMC -> Settings.";
            }
        }
        return throwable.getMessage();
    }
}
