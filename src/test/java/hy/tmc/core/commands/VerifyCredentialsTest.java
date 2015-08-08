package hy.tmc.core.commands;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertThat;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

import hy.tmc.core.communication.authorization.Authorization;
import hy.tmc.core.communication.UrlCommunicator;
import hy.tmc.core.configuration.TmcSettings;
import hy.tmc.core.CoreTestSettings;
import hy.tmc.core.exceptions.TmcCoreException;

import org.hamcrest.CoreMatchers;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;

public class VerifyCredentialsTest {

    private final String testUsername = "test";
    private final String testPassword = "1234";
    CoreTestSettings settings;
    UrlCommunicator comm;
    @Rule public WireMockRule wireMockServer = new WireMockRule();

    @Before
    public void setUp() {
        settings = new CoreTestSettings();
        settings.setServerAddress("http://127.0.0.1:8080/");
        comm = new UrlCommunicator(settings);
    }

    private void wiremockGet(String auth, int status) {
        wireMockServer.stubFor(
                get(urlEqualTo("/user"))
                        .withHeader("Authorization", containing("Basic " + auth))
                        .willReturn(aResponse().withStatus(status)));
    }

    @Test(expected = TmcCoreException.class)
    public void checkData() throws Exception {
        new VerifyCredentials(new CoreTestSettings()).call();
    }

    @Test
    public void canAuthenticateWithTestCredentials() throws TmcCoreException, IOException {
        wiremockGet(Authorization.encode(testUsername + ":" + testPassword), 200);
        settings.setUsername(testUsername);
        settings.setPassword(testPassword);
        String result = executeWithSettings(settings);
        assertThat(result, CoreMatchers.containsString("Auth successful."));
    }

    @Test
    public void cannotAuthenticateWithUnexistantCredentials() throws TmcCoreException, IOException {
        String wrongUsername = "samu";
        String wrongPassword = "salis";
        wiremockGet(Authorization.encode(wrongUsername + ":" + wrongPassword), 400);
        settings.setUsername(wrongUsername);
        settings.setPassword(wrongPassword);
        String result = executeWithSettings(settings);
        assertThat(result, CoreMatchers.containsString("Auth unsuccessful."));
    }

    private String executeWithSettings(TmcSettings settings) throws TmcCoreException, IOException {
        VerifyCredentials auth = new VerifyCredentials(settings, comm);
        auth.checkData();
        return auth.parseData(auth.call()).get();
    }
}
