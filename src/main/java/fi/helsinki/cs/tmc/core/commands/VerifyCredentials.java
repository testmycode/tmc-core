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

    /**
     * Regex for HTTP OK codes.
     */
    private final String httpOk = "2..";

    private String tmcServerRoute = "/user";
    private UrlCommunicator communicator;

    public VerifyCredentials(TmcSettings settings) {
        this(settings, new UrlCommunicator(settings));
    }

    public VerifyCredentials(TmcSettings settings, UrlCommunicator communicator) {
        super(settings);
        this.communicator = communicator;
    }

    @Override
    public void checkData() throws TmcCoreException {
        String username = settings.getUsername();
        if (username == null || username.isEmpty()) {
            throw new TmcCoreException("username must be set!");
        }
        String password = settings.getPassword();
        if (password == null || password.isEmpty()) {
            throw new TmcCoreException("password must be set!");
        }
    }

    private int makeRequest() throws IOException, TmcCoreException {
        String auth = settings.getUsername() + ":" + settings.getPassword();
        int code =
                communicator
                        .makeGetRequest(settings.getServerAddress() + tmcServerRoute, auth)
                        .getStatusCode();
        return code;
    }

    @Override
    public Boolean call() throws TmcCoreException, IOException {
        checkData();
        if (isOk(makeRequest())) {
            return true;
        }
        return false;
    }

    public Optional<String> parseData(Object data) {
        Boolean result = (Boolean) data;
        if (result) {
            return Optional.of("Auth successful. Saved userdata in session");
        }
        return Optional.of("Auth unsuccessful. Check your connection and/or credentials");
    }

    private boolean isOk(int code) {
        return Integer.toString(code).matches(httpOk);
    }
}
