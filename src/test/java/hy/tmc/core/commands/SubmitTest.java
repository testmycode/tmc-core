package hy.tmc.core.commands;


import com.github.tomakehurst.wiremock.client.WireMock;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import hy.tmc.core.communication.CourseSubmitter;
import hy.tmc.core.communication.SubmissionPoller;
import hy.tmc.core.communication.TmcJsonParser;
import hy.tmc.core.testhelpers.ClientTmcSettings;
import hy.tmc.core.domain.Course;
import hy.tmc.core.domain.submission.SubmissionResult;
import hy.tmc.core.exceptions.TmcCoreException;
import hy.tmc.core.testhelpers.ExampleJson;

import java.io.IOException;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class SubmitTest {

    private Submit submit;
    private CourseSubmitter submitterMock;
    private final String submissionUrl = "/submissions/1781.json?api_version=7";
    private ClientTmcSettings settings;

    @Rule
    public WireMockRule wireMock = new WireMockRule();
    
    @Before
    public void setup() throws Exception {
        settings = new ClientTmcSettings();
        settings.setUsername("Samu");
        settings.setPassword("Bossman");
        settings.setCurrentCourse(new Course());
        submitterMock = Mockito.mock(CourseSubmitter.class);
        when(submitterMock.submit(anyString())).thenReturn("http://127.0.0.1:8080" + submissionUrl);
        submit = new Submit(submitterMock, 
                            new SubmissionPoller(new TmcJsonParser(settings)), 
                settings, "polku/kurssi/kansioon/src");
    }

    /**
     * Check that data checking success.
     */
    @Test
    public void testCheckDataSuccess() throws TmcCoreException, IOException {
        Submit submitCommand = new Submit(settings);
        submitCommand.setParameter("path", "/home/tmccli/testi");
        submitCommand.checkData();
    }

    /**
     * Check that if user didn't give correct data, data checking fails.
     */
    @Test(expected = TmcCoreException.class)
    public void testCheckDataFail() throws Exception{
        Submit submitCommand = new Submit(settings);
        submitCommand.checkData();
    }

    @Test(expected = TmcCoreException.class)
    public void checkDataFailIfNoAuth() throws Exception {
        Submit submitCommand = new Submit(new ClientTmcSettings());
        submitCommand.checkData();
    }
    
    @Test
    public void submitReturnsSuccesfulResponse() throws Exception{
        wireMock.stubFor(get(urlEqualTo(submissionUrl))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withBody(ExampleJson.successfulSubmission)));
        
        SubmissionResult submissionResult = submit.call();
        assertFalse(submissionResult == null);
        assertTrue(submissionResult.isAllTestsPassed());
    }
    
    @Test
    public void submitReturnsUnsuccesfulResponse() throws Exception{
        wireMock.stubFor(get(urlEqualTo(submissionUrl))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withBody(ExampleJson.failedSubmission)));
        
        SubmissionResult submissionResult = submit.call();
        assertFalse(submissionResult == null);
        assertFalse(submissionResult.isAllTestsPassed());
    }
}
