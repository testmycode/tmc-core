package fi.helsinki.cs.tmc.core.commands;

import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import static com.github.tomakehurst.wiremock.client.WireMock.verify;

import fi.helsinki.cs.tmc.core.CoreTestSettings;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;
import fi.helsinki.cs.tmc.core.spyware.DiffSender;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Arrays;

public class SendSpywareDiffsTest {

    private SendSpywareDiffs command;
    private DiffSender sender;
    private CoreTestSettings settings;
    private byte[] bytes;

    @Rule public WireMockRule wireMock = new WireMockRule();

    @Before
    public void setUp() {
        this.settings = new CoreTestSettings();
        this.settings.setPassword("password");
        this.settings.setUsername("username");
        this.settings.setCurrentCourse(new Course());

        this.sender = new DiffSender(settings);

        this.bytes = new byte[] {0x01, 0x02, 0x03};

        this.command = new SendSpywareDiffs(bytes, sender, settings);
    }

    @Test(expected = TmcCoreException.class)
    public void testThrowsExceptionIfNoUsername() throws Exception {
        settings.setUsername(null);
        command.call();
    }

    @Test(expected = TmcCoreException.class)
    public void testThrowsExceptionIfNoPassword() throws Exception {
        settings.setPassword(null);
        command.call();
    }

    @Test(expected = TmcCoreException.class)
    public void testThrowsExceptionIfNoCurrentCourse() throws Exception {
        settings.setCurrentCourse(null);
        command.call();
    }

    @Test
    public void testCallSendsDataToSpywareServer() throws Exception {
        wireMock.stubFor(
                post(urlEqualTo("/spyware"))
                        .withRequestBody(matching(new String(bytes)))
                        .willReturn(
                                WireMock.aResponse()
                                        .withStatus(200)
                                        .withBody("SPYWARE TULI PERILLE")));

        Course currentCourse = new Course();
        currentCourse.setSpywareUrls(Arrays.asList("http://localhost:8080/spyware"));
        settings.setCurrentCourse(currentCourse);

        command = new SendSpywareDiffs(bytes, sender, settings);

        command.call();

        verify(postRequestedFor(urlEqualTo("/spyware")));
    }
}
