package fi.helsinki.cs.tmc.core.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import fi.helsinki.cs.tmc.core.communication.TmcServerCommunicationTaskFactory;
import fi.helsinki.cs.tmc.core.communication.oauth2.Oauth;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.exceptions.AuthenticationFailedException;
import fi.helsinki.cs.tmc.core.holders.TmcSettingsHolder;
import fi.helsinki.cs.tmc.core.utils.MockSettings;

import com.google.common.base.Optional;

import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;

public class AuthenticateUserTest {
    @Mock
    ProgressObserver mockObserver;

    TmcSettings settings;
    @Mock
    Oauth oauth;
    @Mock
    TmcServerCommunicationTaskFactory tmcServerCommunicationTaskFactory;

    private Command<Void> command;

    @Before
    public void setUp() throws OAuthProblemException, OAuthSystemException, IOException {
        MockitoAnnotations.initMocks(this);
        settings = new MockSettings();
        TmcSettingsHolder.set(settings);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                settings.setToken(Optional.of("testToken"));
                return null;
            }
        }).when(oauth).fetchNewToken("password");
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                throw new OAuthSystemException();
            }
        }).when(oauth).fetchNewToken("wrongPassword");
    }

    @Test
    public void testCallSucceeds() throws Exception {
        command = new AuthenticateUser(mockObserver, "password",
                                        oauth, tmcServerCommunicationTaskFactory);
        command.call();
        assertTrue(settings.getToken().isPresent());
        assertEquals(settings.getToken().get(), "testToken");
    }

    @Test(expected = AuthenticationFailedException.class)
    public void testCallFails() throws Exception {
        command = new AuthenticateUser(mockObserver, "wrongPassword",
                                        oauth, tmcServerCommunicationTaskFactory);
        command.call();
    }

    @Test
    public void callsTmcServerCommunicationTaskFactorysGetOauthCredentialsTask() throws Exception {
        command = new AuthenticateUser(mockObserver, "password",
                                        oauth, tmcServerCommunicationTaskFactory);
        command.call();
        verify(tmcServerCommunicationTaskFactory, times(1)).fetchOauthCredentialsTask();
    }
}
