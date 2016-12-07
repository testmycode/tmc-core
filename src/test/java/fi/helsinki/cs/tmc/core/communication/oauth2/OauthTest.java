package fi.helsinki.cs.tmc.core.communication.oauth2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class OauthTest {

    private Oauth oauth;

    @Mock
    private OauthFlow flow;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.oauth = new Oauth(flow);
        when(flow.getToken()).thenReturn("one", "two");
    }

    @Test
    public void hasNoTokenWhenInitialized() throws Exception {
        assertFalse(oauth.hasToken());
        verify(flow, times(0)).getToken();
    }

    @Test
    public void hasTokenWhenFetchedAndCallsFlow() {
        try {
            String token = oauth.getToken();
            verify(flow).getToken();
            assertTrue(oauth.hasToken());
            assertEquals("one", token);
        } catch (OAuthSystemException | OAuthProblemException ex) {
            fail("Got exception: " + ex.toString());
        }
    }

    @Test
    public void refreshTokenCallsFlowAndGetsNewToken() {
        try {
            String token = oauth.getToken();
            assertEquals("one", token);
            verify(flow, times(1)).getToken();
            String token2 = oauth.refreshToken();
            assertEquals("two", token2);
            verify(flow, times(2)).getToken();
            assertTrue(oauth.hasToken());
        } catch (OAuthSystemException | OAuthProblemException ex) {
            fail("Got exception: " + ex.toString());
        }
    }
}
