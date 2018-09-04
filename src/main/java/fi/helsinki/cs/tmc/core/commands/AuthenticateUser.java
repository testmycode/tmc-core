package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.TmcServerCommunicationTaskFactory;
import fi.helsinki.cs.tmc.core.communication.oauth2.Oauth;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.domain.UserInfo;
import fi.helsinki.cs.tmc.core.exceptions.AuthenticationFailedException;
import fi.helsinki.cs.tmc.core.exceptions.ShowToUserException;
import fi.helsinki.cs.tmc.core.holders.TmcSettingsHolder;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;

import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class AuthenticateUser extends Command<Void> {
    private String password;
    private final Oauth oauth;
    private static final Logger logger = LoggerFactory.getLogger(AuthenticateUser.class);


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
    public Void call() throws AuthenticationFailedException, IOException, ShowToUserException {
        try {
            tmcServerCommunicationTaskFactory.fetchOauthCredentialsTask();
            oauth.fetchNewToken(password);
            fetchUserInfo();
        } catch (Exception e) {
            if (e instanceof OAuthSystemException || e instanceof OAuthProblemException || e instanceof AuthenticationFailedException) {
                throw new AuthenticationFailedException(e);
            } else if (e instanceof IOException) {
                throw (IOException) e;
            } else if (e instanceof ShowToUserException) {
                throw (ShowToUserException) e;
            } else {
                throw new IOException("Something went wrong while authenticating!", e);
            }
        }
        return null;
    }

    private void fetchUserInfo() throws AuthenticationFailedException {
        TmcSettings settings = TmcSettingsHolder.get();
        UserInfo userInfo;
        try {
            userInfo = tmcServerCommunicationTaskFactory.getUserInfo();
            settings.setId(userInfo.getId());
            settings.setUsername(userInfo.getUsername());
            settings.setEmail(userInfo.getEmail());
        } catch (Exception e) {
            logger.error("Could not get user info", e);
            settings.setToken(Optional.absent());
            throw new AuthenticationFailedException(e);
        }
    }
}
