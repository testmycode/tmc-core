package fi.helsinki.cs.tmc.core.utilities;

import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.exceptions.FailedHttpResponseException;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;
import fi.helsinki.cs.tmc.core.holders.TmcSettingsHolder;

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
                TmcSettings tmcSettings = TmcSettingsHolder.get();
                String errorMessage
                        = "";
                if (tmcSettings.getUsername().isPresent() && tmcSettings.getUsername().get().contains("@")) {
                    return errorMessage
                        + "\nNote that you must log in with your username, not your email address.";
                }
                return errorMessage;
            }
        }
        return throwable.getMessage();
    }
}
