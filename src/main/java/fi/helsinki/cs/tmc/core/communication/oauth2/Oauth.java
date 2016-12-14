package fi.helsinki.cs.tmc.core.communication.oauth2;

import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;

public class Oauth {
    
    private static Oauth oauth;

    private OauthFlow flow;
    private String token;

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
    }

    /**
     * Returns the oauth token.
     *
     * <p>
     * Gets the token from cache or uses the known flow to fetch the it.</p>
     *
     * @return oauth token
     * @throws org.apache.oltu.oauth2.common.exception.OAuthSystemException an error occurred with
     *     getting token
     * @throws org.apache.oltu.oauth2.common.exception.OAuthProblemException an error occurred with
     *     getting token
     */
    public String getToken() throws OAuthSystemException, OAuthProblemException {
        if (flow == null) {
            throw new OAuthSystemException("Oauth flow is null");
        }
        if (!hasToken()) {
            token = flow.getToken();
        }
        return token;
    }

    /**
     * Returns true if the token is already known.
     *
     * @return has token
     */
    public boolean hasToken() {
        return token != null;
    }

    /**
     * Returns the oauth token.
     *
     * <p>
     * Uses the known flow to fetch the token and caches it.</p>
     *
     * @return oauth token
     * @throws org.apache.oltu.oauth2.common.exception.OAuthSystemException an error occurred with
     *     getting token
     * @throws org.apache.oltu.oauth2.common.exception.OAuthProblemException an error occurred with
     *     getting token
     */
    public String refreshToken() throws OAuthSystemException, OAuthProblemException {
        if (flow == null) {
            throw new OAuthSystemException("Oauth flow is null");
        }
        token = flow.getToken();
        return token;
    }
    
    /**
     * Changes the oauth flow.
     * 
     * @param authflow an oauth flow
     */
    public void setFlow(OauthFlow authflow) {
        this.flow = authflow;
    }
}
