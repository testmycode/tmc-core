package fi.helsinki.cs.tmc.core.communication.oauth2;

import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;

/**
 * An interface for an oauth flow.
 */
public interface OauthFlow {

    /**
     * Uses the flow to get a token from the server.
     *
     * @return a oauth token
     * @throws org.apache.oltu.oauth2.common.exception.OAuthSystemException an error occurred with
     *     getting token
     * @throws org.apache.oltu.oauth2.common.exception.OAuthProblemException an error occurred with
     *     getting token
     */
    public String getToken() throws OAuthSystemException, OAuthProblemException;
}
