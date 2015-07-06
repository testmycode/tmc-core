package hy.tmc.core.spyware;

import hy.tmc.core.spyware.DiffSender;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import static org.junit.Assert.assertEquals;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.io.Files;

import hy.tmc.core.communication.HttpResult;
import hy.tmc.core.testhelpers.ClientTmcSettings;
import hy.tmc.core.domain.Course;
import hy.tmc.core.exceptions.TmcCoreException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import static org.junit.Assert.assertNull;
import org.junit.Test;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

public class DiffSenderTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();
    
    @Rule
    public ExpectedException exceptedEx = ExpectedException.none();

    private final String spywareUrl = "http://127.0.0.1:8080/spyware";
    private DiffSender sender;
    private String originalServerUrl;
    private ClientTmcSettings settings;
    
    /**
     * Logins the users and creates fake server.
     */
    @Before
    public void setup() throws IOException {
        settings = new ClientTmcSettings();
        settings.setServerAddress("http://127.0.0.1:8080");
        settings.setUsername("test");
        settings.setPassword("1234");
        sender = new DiffSender(settings);
        startWiremock();
    }

    @Test
    public void testSendToSpywareWithFile() throws IOException, TmcCoreException {
        final File file = new File("testResources/test.zip");
        DiffSender sender = new DiffSender(settings);
        HttpResult res = sender.sendToUrl(file,
                spywareUrl);
        assertEquals(200, res.getStatusCode());
    }

    @Test
    public void testSendToAllUrlsWithFile() throws IOException, TmcCoreException {
        final File file = new File("testResources/test.zip");
        Course testCourse = new Course();
        List<String> urls = new ArrayList<>();
        urls.add(spywareUrl);
        testCourse.setSpywareUrls(
                urls
        );
        List<HttpResult> results = sender.sendToSpyware(file, testCourse);
        for (HttpResult res : results) {
            assertEquals(200, res.getStatusCode());
        }
    }

    @Test
    public void testSendToSpywareWithByteArray() throws IOException, TmcCoreException {
        final File file = new File("testResources/test.zip");
        byte[] byteArray = Files.toByteArray(file);
        DiffSender sender = new DiffSender(settings);
        HttpResult res = sender.sendToUrl(byteArray,
                spywareUrl);
        assertEquals(200, res.getStatusCode());
    }
    
    @Test
    public void requestWithInvalidParams() throws IOException, TmcCoreException {
        final File file = new File("testResources/test.zip");
        byte[] byteArray = Files.toByteArray(file);
        DiffSender sender = new DiffSender(settings);
        HttpResult res = sender.sendToUrl(byteArray,
                "vaaraUrl");
        assertNull(res);
    }
    
    @Test
    public void testSendToAllUrlsWithByteArray() throws IOException, TmcCoreException {
        final File file = new File("testResources/test.zip");
        byte[] byteArray = Files.toByteArray(file);
        Course testCourse = new Course();
        testCourse.setSpywareUrls(
                Arrays.asList(new String[]{spywareUrl})
        );
        List<HttpResult> results = sender.sendToSpyware(byteArray, testCourse);
        for (HttpResult res : results) {
            assertEquals(200, res.getStatusCode());
        }
    }
    
    @Test
    public void spywarePostIncludesFileAndHeaders() throws IOException, TmcCoreException {
        File testFile = new File("testResources/test.zip");
        HttpResult res = sender.sendToUrl(testFile, spywareUrl);
        assertEquals(200, res.getStatusCode());
    }

    private void startWiremock() {
        stubFor(post(urlEqualTo("/spyware"))
                .withHeader("X-Tmc-Version", equalTo("1"))
                .withHeader("X-Tmc-Username", equalTo(settings.getUsername()))
                .withHeader("X-Tmc-Password", equalTo(settings.getPassword()))
                .willReturn(aResponse().withBody("OK").withStatus(200)));
    }
}
