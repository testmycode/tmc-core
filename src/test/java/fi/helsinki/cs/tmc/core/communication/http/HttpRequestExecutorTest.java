package fi.helsinki.cs.tmc.core.communication.http;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.holders.TmcSettingsHolder;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.util.EntityUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.URI;
import java.nio.charset.Charset;

public class HttpRequestExecutorTest {

    @Mock TmcSettings settings;

    @Rule public WireMockRule wireMockRule = new WireMockRule(0);

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        TmcSettingsHolder.set(settings);
        verifyNoMoreInteractions(settings);
        wireMockRule.start();
    }

    @After
    public void tearDown() throws Exception {
        wireMockRule.stop();
    }

    @Test
    public void testDirectReturn() throws Exception {
        wireMockRule.stubFor(
                get(urlEqualTo("/")).willReturn(aResponse().withBody(new byte[] {1, 2, 3})));

        BufferedHttpEntity result = new HttpRequestExecutor(getAddressFor("/")).call();

        assertArrayEquals(new byte[] {1, 2, 3}, EntityUtils.toByteArray(result));
    }

    @Test
    public void testFollowingRedirectsAutomatically() throws Exception {

        wireMockRule.stubFor(
                get(urlEqualTo("/one"))
                        .willReturn(
                                aResponse()
                                        .withHeader("Location", getAddressFor("/two").toString())
                                        .withStatus(302)));

        wireMockRule.stubFor(
                get(urlEqualTo("/two")).willReturn(aResponse().withBody(new byte[] {4, 5, 6})));

        BufferedHttpEntity result = new HttpRequestExecutor(getAddressFor("/one")).call();
        assertArrayEquals(new byte[] {4, 5, 6}, EntityUtils.toByteArray(result));
        wireMockRule.verify(1, WireMock.getRequestedFor(urlEqualTo("/one")));
        wireMockRule.verify(1, WireMock.getRequestedFor(urlEqualTo("/two")));
    }

    @Test
    public void testBasicAuth() throws Exception {
        wireMockRule.stubFor(
                get(urlEqualTo("/auth"))
                        .willReturn(
                                aResponse()
                                        .withHeader("WWW-Authenticate", "Basic realm=\"Test case\"")
                                        .withStatus(401)));

        wireMockRule.stubFor(
                get(urlEqualTo("/auth"))
                        .withHeader(
                                "Authorization",
                                matching(
                                        "Basic "
                                                + Base64.encodeBase64String(
                                                        "theuser:thepassword"
                                                                .getBytes(
                                                                        Charset.forName("UTF-8")))))
                        .willReturn(aResponse().withBody("Yay")));

        URI uri = getAddressFor("auth");
        uri =
                new URI(
                        uri.getScheme(),
                        "theuser:thepassword",
                        uri.getHost(),
                        uri.getPort(),
                        uri.getPath(),
                        uri.getQuery(),
                        uri.getFragment());

        BufferedHttpEntity result = new HttpRequestExecutor(uri).setTimeout(5000).call();
        assertEquals("Yay", EntityUtils.toString(result, "UTF-8"));
    }

    private URI getAddressFor(String path) {
        return URI.create("http://127.0.0.1:" + wireMockRule.port() + "/" + path);
    }
}
