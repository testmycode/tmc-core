package fi.helsinki.cs.tmc.core.commands;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import fi.helsinki.cs.tmc.core.CoreTestSettings;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.communication.ExerciseSubmitter;
import fi.helsinki.cs.tmc.core.communication.SubmissionPoller;
import fi.helsinki.cs.tmc.core.communication.TmcJsonParser;
import fi.helsinki.cs.tmc.core.communication.UrlHelper;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.submission.SubmissionResult;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;
import fi.helsinki.cs.tmc.core.testhelpers.ExampleJson;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SubmitTest {

    private static final String FILE_SEPARATOR = File.separator;
    private final String submissionUrl;

    private Submit submit;
    private ExerciseSubmitter submitterMock;
    private CoreTestSettings settings;

    @Rule public WireMockRule wireMock = new WireMockRule();

    public SubmitTest() {
        settings = new CoreTestSettings();
        settings.setUsername("Samu");
        settings.setPassword("Bossman");
        settings.setCurrentCourse(new Course());
        settings.setApiVersion("7");
        submissionUrl = new UrlHelper(settings).withParams("/submissions/1781.json");
    }

    @Before
    public void setup() throws Exception {
        submitterMock = Mockito.mock(ExerciseSubmitter.class);
        when(submitterMock.submit(anyString())).thenReturn("http://127.0.0.1:8080" + submissionUrl);
        submit =
                new Submit(
                        submitterMock,
                        new SubmissionPoller(new TmcJsonParser(settings)),
                        settings,
                        "polku"
                                + FILE_SEPARATOR
                                + "kurssi"
                                + FILE_SEPARATOR
                                + "kansioon"
                                + FILE_SEPARATOR
                                + "src");
    }

    /**
     * Check that data checking success.
     */
    @Test
    public void testCheckDataSuccess() throws TmcCoreException, IOException {
        Submit submitCommand = new Submit(settings);
        submitCommand.setParameter(
                "path",
                FILE_SEPARATOR + "home" + FILE_SEPARATOR + "tmccli" + FILE_SEPARATOR + "testi");
        submitCommand.checkData();
    }

    /**
     * Check that if user didn't give correct data, data checking fails.
     */
    @Test(expected = TmcCoreException.class)
    public void testCheckDataFail() throws Exception {
        Submit submitCommand = new Submit(settings);
        submitCommand.checkData();
    }

    @Test(expected = TmcCoreException.class)
    public void checkDataFailIfNoAuth() throws Exception {
        Submit submitCommand = new Submit(new CoreTestSettings());
        submitCommand.checkData();
    }

    @Test
    public void submitReturnsSuccesfulResponse() throws Exception {
        wireMock.stubFor(
                get(urlEqualTo(submissionUrl))
                        .willReturn(
                                WireMock.aResponse()
                                        .withStatus(200)
                                        .withBody(ExampleJson.successfulSubmission)));

        SubmissionResult submissionResult = submit.call();
        assertFalse(submissionResult == null);
        assertTrue(submissionResult.isAllTestsPassed());
    }

    @Test
    public void submitReturnsUnsuccesfulResponse() throws Exception {
        wireMock.stubFor(
                get(urlEqualTo(submissionUrl))
                        .willReturn(
                                WireMock.aResponse()
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
        ListenableFuture<SubmissionResult> submit =
                core.submit(
                        "testResources"
                                + FILE_SEPARATOR
                                + "halfdoneExercise"
                                + FILE_SEPARATOR
                                + "viikko1"
                                + FILE_SEPARATOR
                                + "Viikko1_004.Muuttujat",
                        settings);
        final List<SubmissionResult> result = new ArrayList<SubmissionResult>();
        Futures.addCallback(
                submit,
                new FutureCallback<SubmissionResult>() {

                    @Override
                    public void onSuccess(SubmissionResult sub) {
                        result.add(sub);
                    }

                    @Override
                    public void onFailure(Throwable thrwbl) {
                        System.out.println("VIRHE: " + thrwbl);
                        thrwbl.printStackTrace();
                    }
                });
        while (!submit.isDone()) {
            Thread.sleep(100);
        }
        assertFalse(result.isEmpty());
        assertFalse(result.get(0).isAllTestsPassed());
    }

    private void mockSubmit() {
        UrlHelper helper = new UrlHelper(settings);
        String urlToMock = helper.withParams("/exercises/1231/submissions.json");
        System.out.println(urlToMock);
        wireMock.stubFor(
                post(urlEqualTo(urlToMock))
                        .willReturn(
                                WireMock.aResponse()
                                        .withStatus(200)
                                        .withBody(
                                                ExampleJson.failedSubmitResponse.replace(
                                                        "https://tmc.mooc.fi/staging",
                                                        "http://localhost:8080"))));

        urlToMock = helper.withParams("/submissions/7777.json");
        System.out.println(urlToMock);
        wireMock.stubFor(
                get(urlEqualTo(urlToMock))
                        .willReturn(
                                WireMock.aResponse()
                                        .withStatus(200)
                                        .withBody(
                                                ExampleJson.failedSubmission.replace(
                                                        "https://tmc.mooc.fi/staging",
                                                        "http://localhost:8080"))));

        urlToMock = helper.withParams("/courses/19.json");
        System.out.println(urlToMock);
        wireMock.stubFor(
                get(urlEqualTo(urlToMock))
                        .willReturn(
                                WireMock.aResponse()
                                        .withStatus(200)
                                        .withBody(
                                                ExampleJson.noDeadlineCourseExample.replace(
                                                        "https://tmc.mooc.fi/staging",
                                                        "http://localhost:8080"))));
    }
}
