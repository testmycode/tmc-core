package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.TmcServerCommunicationTaskFactory;
import fi.helsinki.cs.tmc.core.communication.oauth2.Oauth;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.exceptions.AuthenticationFailedException;
import fi.helsinki.cs.tmc.core.holders.TmcSettingsHolder;

import com.google.common.annotations.VisibleForTesting;

import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;

import java.io.IOException;

public class AuthenticateUser extends Command<Void> {
    private String password;
    private final Oauth oauth;

    public AuthenticateUser(ProgressObserver observer, String password, Oauth oauth) {
        super(observer);
        this.password = password;
        this.oauth = oauth;
    }

    @VisibleForTesting
    AuthenticateUser(ProgressObserver observer, String password, Oauth oauth,
                      TmcServerCommunicationTaskFactory tmcServerCommunicationTaskFactory) {
        super(observer, tmcServerCommunicationTaskFactory);
        this.password = password;
        this.oauth = oauth;
    }

    @Override
    public Void call() throws AuthenticationFailedException, IOException {
        try {
            tmcServerCommunicationTaskFactory.getOauthCredentialsTask();
            oauth.fetchNewToken(password);
        } catch (OAuthSystemException | OAuthProblemException e) {
            throw new AuthenticationFailedException(e);
        }
        return null;
    }
}
