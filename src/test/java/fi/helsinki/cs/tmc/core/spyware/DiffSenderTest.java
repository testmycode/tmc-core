package fi.helsinki.cs.tmc.core.spyware;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import static org.junit.Assert.assertEquals;

import fi.helsinki.cs.tmc.core.CoreTestSettings;
import fi.helsinki.cs.tmc.core.communication.HttpResult;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

import com.google.common.io.Files;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

public class DiffSenderTest {

    @Rule public WireMockRule wireMockRule = new WireMockRule(0);
    private String serverAddress = "http://127.0.0.1:";

    @Rule public ExpectedException exceptedEx = ExpectedException.none();

    private URI spywareUrl;
    private DiffSender sender;
    private CoreTestSettings settings;

    /**
     * Logins the users and creates fake server.
     */
    @Before
    public void setup() throws IOException {
        settings = new CoreTestSettings();
        settings.setServerAddress("http://127.0.0.1:8080");
        settings.setUsername("test");
        settings.setPassword("1234");

        serverAddress += wireMockRule.port();
        spywareUrl = URI.create(serverAddress + "/spyware");

        sender = new DiffSender(settings);
        startWiremock();
    }

    @Test
    public void testSendToSpywareWithByteArray() throws IOException, TmcCoreException {
        final File file = new File("src/test/resources/test.zip");
        byte[] byteArray = Files.toByteArray(file);

        DiffSender sender = new DiffSender(settings);

        HttpResult res = sender.sendToUrl(byteArray, spywareUrl);
        assertEquals(200, res.getStatusCode());
    }

    @Test
    public void requestWithInvalidParams() throws IOException, TmcCoreException {
        final File file = new File("src/test/resources/test.zip");
        byte[] byteArray = Files.toByteArray(file);

        DiffSender sender = new DiffSender(settings);

        HttpResult res = sender.sendToUrl(byteArray, URI.create("vaaraUrl"));
        assertEquals(500, res.getStatusCode());
    }

    @Test
    public void testSendToAllUrlsWithByteArray() throws IOException, TmcCoreException {
        final File file = new File("src/test/resources/test.zip");
        byte[] byteArray = Files.toByteArray(file);
        Course testCourse = new Course();
        testCourse.setSpywareUrls(Arrays.asList(spywareUrl));
        List<HttpResult> results = sender.sendToSpyware(byteArray, testCourse);
        for (HttpResult res : results) {
            assertEquals(200, res.getStatusCode());
        }
    }

    private void startWiremock() {
        stubFor(
                post(urlEqualTo("/spyware"))
                        .withHeader("X-Tmc-Version", equalTo("1"))
                        .withHeader("X-Tmc-Username", equalTo(settings.getUsername()))
                        .withHeader("X-Tmc-Password", equalTo(settings.getPassword()))
                        .willReturn(aResponse().withBody("OK").withStatus(200)));
    }
}
