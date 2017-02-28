package fi.helsinki.cs.tmc.core.communication.oauth2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.holders.TmcSettingsHolder;
import fi.helsinki.cs.tmc.core.utils.MockSettings;

import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Field;

public class OauthTest {

    private TmcSettings settings;

    private Oauth oauth;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        settings = new MockSettings();
        TmcSettingsHolder.set(settings);
        oauth = spy(new Oauth());
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                settings.setToken("testToken");
                return null;
            }
        }).when(oauth).fetchNewToken(anyString());
    }

    @After
    public void tearDown() throws NoSuchFieldException, IllegalAccessException {
        Field oauth = Oauth.class.getDeclaredField("oauth");
        oauth.setAccessible(true);
        oauth.set(null, null);
    }

    @Test
    public void hasTokenWhenFetched() {
        try {
            oauth.fetchNewToken("password");
            assertTrue(oauth.hasToken());
            assertEquals("testToken", settings.getToken().get());
        } catch (OAuthSystemException | OAuthProblemException ex) {
            fail("Got exception: " + ex.toString());
        }
    }

    @Test
    public void hasNoTokenWhenInitialized() throws Exception {
        assertFalse(oauth.hasToken());
        verify(oauth, times(0)).fetchNewToken(anyString());
    }
}
