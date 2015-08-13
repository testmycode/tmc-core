package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.UrlCommunicator;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;

import com.google.common.base.Optional;

import java.io.IOException;

/**
 * Polls tmc-server (defined in settings) route "/user" with credentials.
 * Tmc-server returns 200, if success and otherwise 401.
 */
public class VerifyCredentials extends Command<Boolean> {

    private static final String TMC_SERVER_ROUTE = "/user";
    private static final int HTTP_SUCCESS_RANGE_MIN = 200;
    private static final int HTTP_SUCCESS_RANGE_MAX = 299;

    private UrlCommunicator communicator;

    public VerifyCredentials(TmcSettings settings, UrlCommunicator communicator) {
        super(settings);
        this.communicator = communicator;
    }

    private void assertHasRequiredData() throws TmcCoreException {
        String username = settings.getUsername();
        if (username == null || username.isEmpty()) {
            throw new TmcCoreException("username must be set!");
        }
        String password = settings.getPassword();
        if (password == null || password.isEmpty()) {
            throw new TmcCoreException("password must be set!");
        }
    }

    @Override
    public Boolean call() throws TmcCoreException, IOException {
        assertHasRequiredData();

        String auth = settings.getUsername() + ":" + settings.getPassword();

        int response =
                communicator
                        .makeGetRequest(settings.getServerAddress() + TMC_SERVER_ROUTE, auth)
                        .getStatusCode();

        return (response >= HTTP_SUCCESS_RANGE_MIN && response <= HTTP_SUCCESS_RANGE_MAX);
    }
}
