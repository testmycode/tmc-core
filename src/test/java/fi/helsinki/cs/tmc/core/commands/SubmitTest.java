package fi.helsinki.cs.tmc.core.commands;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import fi.helsinki.cs.tmc.core.CoreTestSettings;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.communication.ExerciseSubmitter;
import fi.helsinki.cs.tmc.core.communication.SubmissionPoller;
import fi.helsinki.cs.tmc.core.communication.TmcApi;
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
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class SubmitTest {

    private static final String FILE_SEPARATOR = File.separator;

    private Submit submit;
    private ExerciseSubmitter submitterMock;
    private CoreTestSettings settings;
    private String submissionUrl;

    @Rule public WireMockRule wireMock = new WireMockRule();

    @Before
    public void setup() throws Exception {
        settings = new CoreTestSettings();
        settings.setUsername("Samu");
        settings.setPassword("Bossman");
        settings.setCurrentCourse(new Course());
        settings.setApiVersion("7");

        submissionUrl = new UrlHelper(settings).withParams("/submissions/1781.json");

        submitterMock = Mockito.mock(ExerciseSubmitter.class);

        when(submitterMock.submit(anyString())).thenReturn("http://127.0.0.1:8080" + submissionUrl);
        submit =
                new Submit(
                        settings,
                        submitterMock,
                        new SubmissionPoller(new TmcApi(settings)),
                        "polku"
                                + FILE_SEPARATOR
                                + "kurssi"
                                + FILE_SEPARATOR
                                + "kansioon"
                                + FILE_SEPARATOR
                                + "src");
    }

    @Test(expected = TmcCoreException.class)
    public void testThrowsExceptionIfNoUsername() throws Exception {
        settings.setUsername(null);
        new Submit(settings, null, null, "").call();
    }

    @Test(expected = TmcCoreException.class)
    public void testThrowsExceptionIfNoPassword() throws Exception {
        settings.setPassword(null);
        new Submit(settings, null, null, "").call();
    }

    @Test
    public void testHandlesSuccessfulTestRunResponseCorrectly() throws Exception {
        wireMock.stubFor(
                get(urlEqualTo(submissionUrl))
                        .willReturn(
                                WireMock.aResponse()
                                        .withStatus(200)
                                        .withBody(ExampleJson.successfulSubmission)));

        SubmissionResult submissionResult = submit.call();
        assertNotNull(submissionResult);
        assertTrue(submissionResult.isAllTestsPassed());
    }

    @Test
    public void testHandlesUnsuccessfulTestRunResponseCorrectly() throws Exception {
        wireMock.stubFor(
                get(urlEqualTo(submissionUrl))
                        .willReturn(
                                WireMock.aResponse()
                                        .withStatus(200)
                                        .withBody(ExampleJson.failedSubmission)));

        SubmissionResult submissionResult = submit.call();
        assertNotNull(submissionResult);
        assertFalse(submissionResult.isAllTestsPassed());
    }

    //TODO: Move to TmcCoreTest or delete
    @Test
    public void submitWithTmcCore() throws Exception {
        buildWireMock();

        CoreTestSettings settings = new CoreTestSettings("test", "1234", "http://localhost:8080");
        TmcApi tmcApi = new TmcApi(settings);
        Course course = tmcApi.getCourseFromString(ExampleJson.noDeadlineCourseExample);
        settings.setCurrentCourse(course);
        TmcCore core = new TmcCore(settings);

        Path path = Paths.get(
                "testResources", "halfdoneExercise", "viikko1", "Viikko1_004.Muuttujat");
        ListenableFuture<SubmissionResult> submit = core.submit(path);
        final List<SubmissionResult> result = new ArrayList<>();
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

    private void buildWireMock() throws URISyntaxException {
        wireMock.stubFor(
                post(urlPathEqualTo("/exercises/1231/submissions.json"))
                        .willReturn(
                                WireMock.aResponse()
                                        .withStatus(200)
                                        .withBody(
                                                ExampleJson.failedSubmitResponse.replace(
                                                        "https://tmc.mooc.fi/staging",
                                                        "http://localhost:8080"))));

        wireMock.stubFor(
                get(urlPathEqualTo("/submissions/7777.json"))
                        .willReturn(
                                WireMock.aResponse()
                                        .withStatus(200)
                                        .withBody(
                                                ExampleJson.failedSubmission.replace(
                                                        "https://tmc.mooc.fi/staging",
                                                        "http://localhost:8080"))));

        wireMock.stubFor(
                get(urlPathEqualTo("/courses/19.json"))
                        .willReturn(
                                WireMock.aResponse()
                                        .withStatus(200)
                                        .withBody(
                                                ExampleJson.noDeadlineCourseExample.replace(
                                                        "https://tmc.mooc.fi/staging",
                                                        "http://localhost:8080"))));
    }
}
