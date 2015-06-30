package hy.tmc.cli.backend.communication;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertEquals;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.base.Optional;
import hy.tmc.cli.configuration.ClientData;
import hy.tmc.cli.frontend.communication.server.ProtocolException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.message.BasicNameValuePair;
import org.junit.After;
import org.junit.Before;

import org.junit.Rule;
import org.junit.Test;

public class URLCommunicatorTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();

    @Test
    public void okWithValidParams() throws IOException, ProtocolException {
        new UrlCommunicator();
        HttpResult result = UrlCommunicator.makeGetRequest("http://127.0.0.1:8080", "test:1234");
        assertEquals(200, result.getStatusCode());
    }

    @Before
    public void setUpWireMock() {
        stubFor(get(urlEqualTo("/"))
                .withHeader("Authorization", containing("Basic dGVzdDoxMjM0"))
                .willReturn(
                        aResponse()
                        .withStatus(200)
                )
        );
        stubFor(get(urlEqualTo("/vaaraurl"))
                .withHeader("Authorization", containing("Basic dGVzdDoxMjM0"))
                .willReturn(
                        aResponse()
                        .withStatus(400)
                )
        );
        stubFor(post(urlEqualTo("/kivaurl"))
                .withHeader("Authorization", containing("Basic dGVzdDoxMjM0"))
                .withRequestBody(containing("submission[file]"))
                .withRequestBody(containing("test.zip"))
                .willReturn(
                        aResponse()
                        .withBody("All tests passed")
                        .withStatus(200)
                )
        );
        stubFor(put(urlEqualTo("/putty"))
                .withHeader("Authorization", containing("Basic dGVzdDoxMjM0"))
                .willReturn(
                        aResponse()
                        .withBody("OK")
                        .withStatus(200)
                )
        );
        stubFor(put(urlEqualTo("/putty_with_headers"))
                .withHeader("Authorization", containing("Basic dGVzdDoxMjM0"))
                .withRequestBody(containing(new BasicNameValuePair("mark_as_read", "1").toString()))
                .willReturn(
                        aResponse()
                        .withBody("OK")
                        .withStatus(200)
                )
        );
    }

    @Test
    public void badRequestWithoutValidURL() throws IOException, ProtocolException {
        HttpResult result = UrlCommunicator.makeGetRequest("http://127.0.0.1:8080/vaaraurl", "test:1234");
        assertEquals(400, result.getStatusCode());
    }

    @Test
    public void notFoundWithoutValidParams() throws IOException, ProtocolException {
        HttpResult result = UrlCommunicator.makeGetRequest("http://127.0.0.1:8080/", "ihanvaaraheaderi:1234");
        assertEquals(403, result.getStatusCode());
    }

    @Test
    public void httpPostAddsFileToRequest() throws IOException, ProtocolException {
        ClientData.setUserData("test", "1234");
        File testFile = new File("testResources/test.zip");
        HttpResult result = UrlCommunicator.makePostWithFile(
                new FileBody(testFile),
                "http://127.0.0.1:8080/kivaurl",
                new HashMap<String, String>());

        ClientData.clearUserData();
        assertEquals("All tests passed", result.getData());
    }

    @Test(expected = IOException.class)
    public void badGetRequestIsThrown() throws IOException, ProtocolException {
        HttpResult makeGetRequest = UrlCommunicator.makeGetRequest("asasdasd", "chang:/\\\\eiparas");
        assertEquals(UrlCommunicator.BAD_REQUEST, makeGetRequest.getStatusCode());
    }

    @Test
    public void makePutRequestSendsPut() throws IOException, ProtocolException {
        ClientData.setUserData("test", "1234");
        Map<String, String> body = new HashMap<>();
        body.put("mark_as_read", "1");
        HttpResult makePutRequest = UrlCommunicator.makePutRequest("http://127.0.0.1:8080/putty", Optional.of(body));
        assertEquals(200, makePutRequest.getStatusCode());
    }

    @Test
    public void makePutRequestHasCorrectHeaders() throws IOException, ProtocolException {
        ClientData.setUserData("test", "1234");
        Map<String, String> body = new HashMap<>();
        body.put("mark_as_read", "1");
        HttpResult makePutRequest = UrlCommunicator.makePutRequest("http://127.0.0.1:8080/putty_with_headers", Optional.of(body));
        assertEquals(200, makePutRequest.getStatusCode());
    }

    @After
    public void clearUser() {
        ClientData.clearUserData();
    }
}
