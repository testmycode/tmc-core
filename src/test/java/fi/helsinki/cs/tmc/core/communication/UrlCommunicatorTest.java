package fi.helsinki.cs.tmc.core.communication;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import static org.junit.Assert.assertEquals;

import fi.helsinki.cs.tmc.core.CoreTestSettings;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

import com.google.common.base.Optional;

import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.message.BasicNameValuePair;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UrlCommunicatorTest {

    private CoreTestSettings settings = new CoreTestSettings();
    private UrlCommunicator urlCommunicator;

    @Rule public WireMockRule wireMockRule = new WireMockRule();

    @Before
    public void setUpWireMock() {
        stubFor(
                get(urlEqualTo("/"))
                        .withHeader("Authorization", containing("Basic dGVzdDoxMjM0"))
                        .willReturn(aResponse().withStatus(200)));
        stubFor(
                get(urlEqualTo("/vaaraurl"))
                        .withHeader("Authorization", containing("Basic dGVzdDoxMjM0"))
                        .willReturn(aResponse().withStatus(400)));
        stubFor(
                post(urlEqualTo("/kivaurl"))
                        .withHeader("Authorization", containing("Basic dGVzdDoxMjM0"))
                        .withRequestBody(containing("submission[file]"))
                        .withRequestBody(containing("test.zip"))
                        .willReturn(aResponse().withBody("All tests passed").withStatus(200)));
        stubFor(
                put(urlEqualTo("/putty?api_version=7&client=tmc_cli&client_version=1"))
                        .withHeader("Authorization", containing("Basic dGVzdDoxMjM0"))
                        .willReturn(aResponse().withBody("OK").withStatus(200)));
        stubFor(
                put(urlEqualTo("/putty_with_headers?api_version=7&client=tmc_cli&client_version=1"))
                        .withHeader("Authorization", containing("Basic dGVzdDoxMjM0"))
                        .withRequestBody(
                                containing(new BasicNameValuePair("mark_as_read", "1").toString()))
                        .willReturn(aResponse().withBody("OK").withStatus(200)));

        urlCommunicator = new UrlCommunicator(settings);
    }

    @Test
    public void okWithValidParams() throws IOException, TmcCoreException {
        HttpResult result = urlCommunicator.makeGetRequest("http://127.0.0.1:8080", "test:1234");
        assertEquals(200, result.getStatusCode());
    }

    @Test
    public void badRequestWithoutValidUrl() throws IOException, TmcCoreException {
        HttpResult result =
                urlCommunicator.makeGetRequest("http://127.0.0.1:8080/vaaraurl", "test:1234");
        assertEquals(400, result.getStatusCode());
    }

    @Test
    public void notFoundWithoutValidParams() throws IOException, TmcCoreException {
        HttpResult result =
                urlCommunicator.makeGetRequest("http://127.0.0.1:8080/", "ihanvaaraheaderi:1234");
        assertEquals(403, result.getStatusCode());
    }

    @Test
    public void httpPostAddsFileToRequest() throws IOException, TmcCoreException {
        settings.setUsername("test");
        settings.setPassword("1234");
        File testFile = new File("testResources/test.zip");
        HttpResult result =
                urlCommunicator.makePostWithFile(
                        new FileBody(testFile),
                        "http://127.0.0.1:8080/kivaurl",
                        new HashMap<String, String>());

        assertEquals("All tests passed", result.getData());
    }

    @Test
    public void httpPostAddsCommentToRequest() throws IOException, TmcCoreException {
        settings.setUsername("test");
        settings.setPassword("1234");
        File testFile = new File("testResources/test.zip");
        HashMap<String, String> params = new HashMap<>();
        params.put("paste", "Commentti");
        HttpResult result =
                urlCommunicator.makePostWithFileAndParams(
                        new FileBody(testFile),
                        "http://127.0.0.1:8080/kivaurl",
                        new HashMap<String, String>(),
                        params);

        assertEquals("All tests passed", result.getData());
    }

    @Test(expected = IOException.class)
    public void badGetRequestIsThrown() throws IOException, TmcCoreException {
        urlCommunicator.makeGetRequest("asasdasd", "chang:/\\\\eiparas");
    }

    @Test
    public void makePutRequestSendsPut() throws IOException, TmcCoreException {
        settings.setUsername("test");
        settings.setPassword("1234");
        Map<String, String> body = new HashMap<>();
        body.put("mark_as_read", "1");
        HttpResult makePutRequest =
                urlCommunicator.makePutRequest("http://127.0.0.1:8080/putty", Optional.of(body));
        assertEquals(200, makePutRequest.getStatusCode());
    }

    @Test
    public void makePutRequestHasCorrectHeaders() throws IOException, TmcCoreException {
        settings.setUsername("test");
        settings.setPassword("1234");
        Map<String, String> body = new HashMap<>();
        body.put("mark_as_read", "1");
        HttpResult makePutRequest =
                urlCommunicator.makePutRequest(
                        "http://127.0.0.1:8080/putty_with_headers", Optional.of(body));
        assertEquals(200, makePutRequest.getStatusCode());
    }
}
