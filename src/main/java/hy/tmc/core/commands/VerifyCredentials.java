package hy.tmc.core.commands;

import com.google.common.base.Optional;

import hy.tmc.core.communication.UrlCommunicator;
import hy.tmc.core.configuration.TmcSettings;

import hy.tmc.core.exceptions.TmcCoreException;
import java.io.IOException;

public class VerifyCredentials extends Command<Boolean> {

    /**
     * Regex for HTTP OK codes.
     */
    private final String httpOk = "2..";

    public VerifyCredentials(String username, String password, TmcSettings settings) {
        super(settings);
        this.setParameter("username", username);
        this.setParameter("password", password);
    }
    
    public VerifyCredentials(TmcSettings settings) {
        super(settings);
    }

    @Override
    public final void setParameter(String key, String value) {
        getData().put(key, value);
    }

    @Override
    public void checkData() throws TmcCoreException {
        String username = this.data.get("username");
        if (username == null || username.isEmpty()) {
            throw new TmcCoreException("username must be set!");
        }
        String password = this.data.get("password");
        if (password == null || password.isEmpty()) {
            throw new TmcCoreException("password must be set!");
        }
    }

    private int makeRequest() throws IOException, TmcCoreException {
        String auth = data.get("username") + ":" + data.get("password");
        int code = new UrlCommunicator(this.settings).makeGetRequest(
                settings.getServerAddress(),
                auth
        ).getStatusCode();
        return code;
    }

    @Override
    public Boolean call() throws TmcCoreException, IOException {
        checkData();
        if (isOk(makeRequest())) {
            //ClientData.setUserData(data.get("username"), data.get("password"));
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
