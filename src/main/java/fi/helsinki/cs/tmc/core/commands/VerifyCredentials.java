package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.UrlCommunicator;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;

import com.google.common.base.Strings;

import java.io.IOException;
import java.net.URI;

/**
 * A {@link Command} for authenticating the user details saved in {@link TmcSettings}.
 */
public class VerifyCredentials extends Command<Boolean> {

    private static final String TMC_SERVER_ROUTE = "/user";
    private static final int HTTP_SUCCESS_RANGE_MIN = 200;
    private static final int HTTP_SUCCESS_RANGE_MAX = 299;

    private UrlCommunicator communicator;

    /**
     * Constructs a new verify credentials command that authenticates user credentials specified
     * in {@code settings} using {@code communicator}.
     */
    public VerifyCredentials(TmcSettings settings, UrlCommunicator communicator) {
        super(settings);
        this.communicator = communicator;
    }

    private void assertHasRequiredData() throws TmcCoreException {
        checkUsername();
        checkPassword();
    }

    private void checkPassword() throws TmcCoreException {
        if (isBadString(settings.getPassword())) {
            throw new TmcCoreException("Cannot verify credentials when no password is set.");
        }
    }

    private void checkUsername() throws TmcCoreException {
        if (isBadString(settings.getUsername())) {
            throw new TmcCoreException("Cannot verify credentials when no username is set.");
        }
    }

    private boolean isBadString(String toBeTested) {
        return Strings.isNullOrEmpty(toBeTested);
    }

    /**
     * Entry point for launching this command.
     */
    @Override
    public Boolean call() throws TmcCoreException, IOException {
        assertHasRequiredData();

        String auth = settings.getUsername() + ":" + settings.getPassword();

        int response = communicator
                .makeGetRequest(URI.create(settings.getServerAddress() + TMC_SERVER_ROUTE), auth)
                .getStatusCode();

        return isStatusCodeSuccess(response);
    }

    private boolean isStatusCodeSuccess(int response) {
        return response >= HTTP_SUCCESS_RANGE_MIN && response <= HTTP_SUCCESS_RANGE_MAX;
    }
}
