package hy.tmc.core.commands;

import com.google.common.base.Optional;
import com.google.common.base.Strings;

import hy.tmc.core.communication.UrlCommunicator;
import hy.tmc.core.configuration.TmcSettings;
import hy.tmc.core.exceptions.TmcCoreException;

import java.io.IOException;

/**
 * Polls TMC-server (defined in settings) route "/user" with credentials.
 */
public class VerifyCredentials extends Command<Boolean> {

    /**
     * Regular expression for HTTP OK codes.
     */
    private final String httpOk = "2..";
    private String tmcServerUserRoute = "/user";
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
        if (Strings.isNullOrEmpty(settings.getUsername())) {
            throw new TmcCoreException("username must be set!");
        }
        if (Strings.isNullOrEmpty(settings.getPassword())) {
            throw new TmcCoreException("password must be set!");
        }
    }

    private int makeRequest() throws IOException, TmcCoreException {
        String auth = settings.getUsername() + ":" + settings.getPassword();
        int code = communicator.makeGetRequest(
                settings.getServerAddress() + tmcServerUserRoute,
                auth
        ).getStatusCode();
        return code;
    }

    @Override
    public Boolean call() throws TmcCoreException, IOException {
        checkData();
        return isOk(makeRequest());
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
