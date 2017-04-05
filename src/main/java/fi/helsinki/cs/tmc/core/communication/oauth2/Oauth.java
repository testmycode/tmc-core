package fi.helsinki.cs.tmc.core.communication.oauth2;

import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.OauthCredentials;
import fi.helsinki.cs.tmc.core.exceptions.NotLoggedInException;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;
import fi.helsinki.cs.tmc.core.holders.TmcSettingsHolder;

import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Oauth {
    
    private static Oauth oauth;
    private static final Logger log = LoggerFactory.getLogger(Oauth.class);

    private TmcSettings settings;
    /**
     * Returns the Oauth instance.
     * 
     * @return single oauth instance
     */
    public static synchronized Oauth getInstance() {
        if (oauth == null) {
            oauth = new Oauth();
        }
        return oauth;
    }
    
    protected Oauth() {
        settings = TmcSettingsHolder.get();
    }

    /**
     * Returns the oauth token.
     *
     * <p>
     * Gets the token from cache or uses the known flow to fetch the it.</p>
     *
     * @return oauth token
     * @throws TmcCoreException when an oauth token hasn't been fetched yet
     */
    public String getToken() throws NotLoggedInException {
        if (!hasToken()) {
            throw new NotLoggedInException();
        }
        return settings.getToken().get();
    }

    /**
     * Returns true if the token is already known.
     *
     * @return has token
     */
    public boolean hasToken() {
        return settings.getToken().isPresent();
    }

    /**
     * Fetches a new oauth token from server using settings' parameters in request.
     * @param password for fetching correct token
     * @throws OAuthSystemException an error occurred with getting token
     * @throws OAuthProblemException an error occurred with getting token
     */
    public void fetchNewToken(String password) throws OAuthSystemException, OAuthProblemException {
        log.info("Fetching new oauth token from server");

        String oauthTokenUrl;
        if (settings.getServerAddress().endsWith("/")) {
            oauthTokenUrl = settings.getServerAddress() + "oauth/token";
        } else {
            oauthTokenUrl = settings.getServerAddress() + "/oauth/token";
        }

        OauthCredentials credentials = settings.getOauthCredentials();
        OAuthClientRequest request = OAuthClientRequest
                .tokenLocation(oauthTokenUrl)
                .setGrantType(GrantType.PASSWORD)
                .setClientId(credentials.getOauthApplicationId())
                .setClientSecret(credentials.getOauthSecret())
                .setUsername(settings.getUsername())
                .setPassword(password)
                .setRedirectURI("urn:ietf:wg:oauth:2.0:oob")
                .buildQueryMessage();
        OAuthClient client = new OAuthClient(new URLConnectionClient());
        String token = client.accessToken(request, OAuthJSONAccessTokenResponse.class)
                .getAccessToken();
        settings.setToken(token);
    }
}
