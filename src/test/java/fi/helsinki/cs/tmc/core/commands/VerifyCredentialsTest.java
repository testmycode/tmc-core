package fi.helsinki.cs.tmc.core.commands;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.core.CoreTestSettings;
import fi.helsinki.cs.tmc.core.communication.UrlCommunicator;
import fi.helsinki.cs.tmc.core.communication.authorization.Authorization;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;

public class VerifyCredentialsTest {

    private final String testUsername = "test";
    private final String testPassword = "1234";
    private final String authString = Authorization.encode(testUsername + ":" + testPassword);
    private CoreTestSettings settings;
    private UrlCommunicator comm;

    @Rule public WireMockRule wireMockServer = new WireMockRule(0);
    private String serverAddress = "http://127.0.0.1:";

    @Before
    public void setUp() {
        serverAddress += wireMockServer.port();
        settings = new CoreTestSettings();
        settings.setServerAddress(serverAddress);
        comm = new UrlCommunicator(settings);
    }

    @Test(expected = TmcCoreException.class)
    public void callThrowsExceptionWhenNoUsername() throws Exception {
        settings.setPassword(testPassword);
        new VerifyCredentials(settings, new UrlCommunicator(settings)).call();
    }

    @Test(expected = TmcCoreException.class)
    public void callThrowsExceptionWhenNoPassword() throws Exception {
        settings.setPassword(testPassword);
        new VerifyCredentials(settings, new UrlCommunicator(settings)).call();
    }

    @Test
    public void canAuthenticateWithTestCredentials() throws TmcCoreException, IOException {
        wireMockServer.stubFor(
                get(urlEqualTo("/user"))
                        .withHeader("Authorization", containing("Basic " + authString))
                        .willReturn(aResponse().withStatus(200)));
        settings.setUsername(testUsername);
        settings.setPassword(testPassword);
        assertTrue(new VerifyCredentials(settings, comm).call());
    }

    @Test
    public void cannotAuthenticateWithUnexistantCredentials() throws TmcCoreException, IOException {
        wireMockServer.stubFor(
                get(urlEqualTo("/user"))
                        .withHeader("Authorization", containing("Basic " + authString))
                        .willReturn(aResponse().withStatus(400)));
        settings.setUsername(testUsername);
        settings.setPassword(testUsername);
        assertFalse(new VerifyCredentials(settings, comm).call());
    }
}
