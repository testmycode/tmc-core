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

    @Mock
    TmcSettings settings;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(0);

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
        wireMockRule.stubFor(get(urlEqualTo("/"))
            .willReturn(aResponse()
                .withBody(new byte[]{1, 2, 3})));

        BufferedHttpEntity result = new HttpRequestExecutor(getAddressFor("/")).call();

        assertArrayEquals(new byte[]{1, 2, 3}, EntityUtils.toByteArray(result));
    }

    // TODO: decide what to do with the BgTaskListener -- replaced with the listener thingy?

//    @Test
//    public void testMultipleCalls() throws Exception {
//        server.setHandler(oneTwoThreeHandler());
//        server.start();
//        MockBgTaskListener<BufferedHttpEntity> listener1 = new MockBgTaskListener<BufferedHttpEntity>();
//        MockBgTaskListener<BufferedHttpEntity> listener2 = new MockBgTaskListener<BufferedHttpEntity>();
//        MockBgTaskListener<BufferedHttpEntity> listener3 = new MockBgTaskListener<BufferedHttpEntity>();
//        Future<BufferedHttpEntity> result1 = BgTask.start("1", new HttpRequestExecutor(server.getBaseUrl()), listener1);
//        Future<BufferedHttpEntity> result2 = BgTask.start("2", new HttpRequestExecutor(server.getBaseUrl()), listener2);
//        Future<BufferedHttpEntity> result3 = BgTask.start("3", new HttpRequestExecutor(server.getBaseUrl()), listener3);
//
//        assertArrayEquals(new byte[]{1, 2, 3}, EntityUtils.toByteArray(result3.get()));
//        assertArrayEquals(new byte[]{1, 2, 3}, EntityUtils.toByteArray(result2.get()));
//        assertArrayEquals(new byte[]{1, 2, 3}, EntityUtils.toByteArray(result1.get()));
//        listener1.waitForCall();
//        listener2.waitForCall();
//        listener3.waitForCall();
//        listener1.assertGotSuccess();
//        listener2.assertGotSuccess();
//        listener3.assertGotSuccess();
//    }


    @Test
    public void testFollowingRedirectsAutomatically() throws Exception {

        wireMockRule.stubFor(get(urlEqualTo("/one")).willReturn(aResponse()
            .withHeader("Location", getAddressFor("/two"))
            .withStatus(302)
        ));

        wireMockRule.stubFor(get(urlEqualTo("/two")).willReturn(aResponse()
            .withBody(new byte[]{4, 5, 6}))
        );

        BufferedHttpEntity result = new HttpRequestExecutor(getAddressFor("/one")).call();
        assertArrayEquals(new byte[]{4, 5, 6}, EntityUtils.toByteArray(result));
        wireMockRule.verify(1, WireMock.getRequestedFor(urlEqualTo("/one")));
        wireMockRule.verify(1, WireMock.getRequestedFor(urlEqualTo("/two")));
    }

    @Test
    public void testBasicAuth() throws Exception {
        wireMockRule.stubFor(get(urlEqualTo("/auth"))
            .willReturn(aResponse()
                .withHeader("WWW-Authenticate", "Basic realm=\"Test case\"")
                .withStatus(401)
            )
        );

        wireMockRule.stubFor(get(urlEqualTo("/auth"))
            .withHeader("Authorization", matching("Basic " + Base64.encodeBase64String("theuser:thepassword".getBytes(Charset.forName("UTF-8")))))
            .willReturn(aResponse().withBody("Yay"))
        );

        URI uri = URI.create(getAddressFor("auth"));
        uri = new URI(uri.getScheme(), "theuser:thepassword", uri.getHost(), uri.getPort(), uri.getPath(), uri.getQuery(), uri.getFragment());

        BufferedHttpEntity result = new HttpRequestExecutor(uri.toString()).setTimeout(5000).call();
        assertEquals("Yay", EntityUtils.toString(result, "UTF-8"));
    }

    private String getAddressFor(String path) {
        return "http://127.0.0.1:" + wireMockRule.port() + "/" + path;
    }
}
