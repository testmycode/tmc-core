package fi.helsinki.cs.tmc.core.communication.oauth2;

import fi.helsinki.cs.tmc.core.configuration.TmcSettings;

import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A password flow for oauth.
 *
 * <p>
 * Password flow fetches the token from server by using username and password</p>
 */
public class PasswordFlow implements OauthFlow {

    private static final Logger LOG = Logger.getLogger(PasswordFlow.class.getName());
    private final TmcSettings settings;

    public PasswordFlow(TmcSettings settings) {
        this.settings = settings;
    }

    @Override
    public String getToken() throws OAuthSystemException, OAuthProblemException {
        LOG.log(Level.INFO, "Fetching new oauth token from server");
        OAuthClientRequest request = OAuthClientRequest
                .tokenLocation(settings.getOauthTokenUrl())
                .setGrantType(GrantType.PASSWORD)
                .setClientId(settings.getOauthApplicationId())
                .setClientSecret(settings.getOauthSecret())
                .setUsername(settings.getUsername())
                .setPassword(settings.getPassword())
                .buildQueryMessage();
        OAuthClient client = new OAuthClient(new URLConnectionClient());
        String token = client.accessToken(request, OAuthJSONAccessTokenResponse.class)
                .getAccessToken();
        return token;
    }

}
