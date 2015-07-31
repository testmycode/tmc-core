package hy.tmc.core.commands;


import com.github.tomakehurst.wiremock.client.WireMock;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import hy.tmc.core.TmcCore;
import hy.tmc.core.communication.ExerciseSubmitter;
import hy.tmc.core.communication.SubmissionPoller;
import hy.tmc.core.communication.TmcJsonParser;
import hy.tmc.core.CoreTestSettings;
import hy.tmc.core.domain.Course;
import hy.tmc.core.domain.submission.SubmissionResult;
import hy.tmc.core.exceptions.TmcCoreException;
import hy.tmc.core.testhelpers.ExampleJson;
import java.io.File;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
    private ExerciseSubmitter submitterMock;
    private final String submissionUrl = "/submissions/1781.json?api_version=7";
    private CoreTestSettings settings;

    @Rule
    public WireMockRule wireMock = new WireMockRule();
    String v = File.separator;
    
    @Before
    public void setup() throws Exception {
        settings = new CoreTestSettings();
        settings.setUsername("Samu");
        settings.setPassword("Bossman");
        settings.setCurrentCourse(new Course());
        submitterMock = Mockito.mock(ExerciseSubmitter.class);
        when(submitterMock.submit(anyString())).thenReturn("http://127.0.0.1:8080" + submissionUrl);
        submit = new Submit(submitterMock, 
                            new SubmissionPoller(new TmcJsonParser(settings)), 
                settings, "polku"+v+"kurssi"+v+"kansioon"+v+"src");
    }

    /**
     * Check that data checking success.
     */
    @Test
    public void testCheckDataSuccess() throws TmcCoreException, IOException {
        Submit submitCommand = new Submit(settings);
        submitCommand.setParameter("path", v+"home"+v+"tmccli"+v+"testi");
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
        Submit submitCommand = new Submit(new CoreTestSettings());
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
    
    @Test
    public void submitWithTmcCore() throws Exception {
        mockSubmit();
        
        TmcCore core = new TmcCore();
        CoreTestSettings settings = new CoreTestSettings("test", "1234", "http://localhost:8080");
        TmcJsonParser parser = new TmcJsonParser(settings);
        Course course = parser.getCourseFromString(ExampleJson.noDeadlineCourseExample);
        settings.setCurrentCourse(course);
        ListenableFuture<SubmissionResult> submit = core.submit(
                "testResources"+v+"halfdoneExercise"+v+"viikko1"+v+"Viikko1_004.Muuttujat",
                settings
        );
        final List<SubmissionResult> result = new ArrayList<SubmissionResult>();
        Futures.addCallback(submit, new FutureCallback<SubmissionResult>() {

            @Override
            public void onSuccess(SubmissionResult sub) {
                result.add(sub);
            }

            @Override
            public void onFailure(Throwable thrwbl) {
                System.out.println("VIRHE: "+ thrwbl);
                thrwbl.printStackTrace();
            }
        });
        while(!submit.isDone()) {
            Thread.sleep(100);
        }
        assertFalse(result.isEmpty());
        assertFalse(result.get(0).isAllTestsPassed());
    }

    private void mockSubmit() {
        String urlToMock = "/exercises/1239/submissions.json?api_version=7";
        wireMock.stubFor(post(urlEqualTo(urlToMock))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withBody(ExampleJson.failedSubmitResponse)));
        
        wireMock.stubFor(get(urlEqualTo("/submissions/7777.json?api_version=7"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withBody(ExampleJson.failedSubmission)));
        
        wireMock.stubFor(get(urlEqualTo("/courses/19.json?api_version=7"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withBody(ExampleJson.noDeadlineCourseExample)));
    }
}
