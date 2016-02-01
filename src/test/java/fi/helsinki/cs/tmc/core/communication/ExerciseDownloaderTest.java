package fi.helsinki.cs.tmc.core.communication;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import fi.helsinki.cs.tmc.core.CoreTestSettings;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;
import fi.helsinki.cs.tmc.langs.util.TaskExecutor;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

import org.apache.commons.io.FileUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class ExerciseDownloaderTest {

    @Rule public WireMockRule wireMockRule = new WireMockRule(0);

    private String serverAddress = "http://127.0.0.1:";

    private ArrayList<Exercise> exercises;
    private ExerciseDownloader exDl;
    private CoreTestSettings settings;
    private Path zipDestination;
    private Exercise modelSolutionExample;

    /**
     * Creates required stubs and example data for downloader.
     */
    @Before
    public void setup() {
        settings = new CoreTestSettings();

        serverAddress += wireMockRule.port();

        exDl = new ExerciseDownloader(new UrlCommunicator(settings), new TmcApi(settings));
        exercises = new ArrayList<>();

        zipDestination = Paths.get("src", "test", "resources", "__files");

        modelSolutionExample = new Exercise();
        modelSolutionExample.setSolutionDownloadUrl(URI.create(serverAddress + "/model"));

        Exercise e1 = new Exercise();
        e1.setZipUrl(URI.create(serverAddress + "/ex1.zip"));
        e1.setName("Exercise1");
        exercises.add(e1);

        Exercise e2 = new Exercise();
        e2.setZipUrl(URI.create(serverAddress + "/ex2.zip"));
        e2.setName("Exercise2");
        exercises.add(e2);

        Exercise e3 = new Exercise();
        e3.setZipUrl(URI.create(serverAddress + "/ex3.zip"));
        e3.setName("Exercise3");
        exercises.add(e3);

        String testZipPath = "testzip.zip";

        stubFor(
                get(urlEqualTo("/ex1.zip"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "text/xml")
                                        .withBodyFile(testZipPath)));

        stubFor(
                get(urlEqualTo("/ex1.zip"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "text/xml")
                                        .withBodyFile(testZipPath)));

        wireMockRule.stubFor(
                get(urlEqualTo("/ex2.zip"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "text/xml")
                                        .withBody("<response>Exercise 2</response>")));

        wireMockRule.stubFor(
                get(urlEqualTo("/ex3.zip"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "text/xml")
                                        .withBody("<response>Exercise 3</response>")));

        String response =
                "{\"api_version\":7,\"course\":{\"id\":21,\"name\":\"k2015-tira\","
                        + "\"details_url\":\"https://example.com/staging/courses/21.json\""
                        + ",\"unlock_url\":\"https://example.com/staging/courses/21/unlock"
                        + ".json\",\"reviews_url\":\"https://example.com/staging/courses"
                        + "/21/reviews"
                        + ".json\",\"comet_url\":\"https://example.com:8443/"
                        + "comet\",\"spyware_urls\":[\"http://staging.spyware."
                        + "testmycode.net/\"],\"unlockables\":[],\"exercises\":[]}}";
        stubFor(
                get(urlEqualTo("/emptyCourse.json"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(response)));

        settings.setUsername("pihla");
        settings.setPassword("juuh");
    }

    @After
    public void remove() {
        Paths.get("src", "test", "resources", "__files", "testfile.txt").toFile().delete();
    }

    @Test
    public void downloadExercisesDoesRequests() throws IOException {
        exDl.downloadExercises(exercises, zipDestination, "", ExerciseObserver.NOP);
        wireMockRule.verify(getRequestedFor(urlEqualTo("/ex1.zip")));
        wireMockRule.verify(getRequestedFor(urlEqualTo("/ex2.zip")));
    }

    @Test
    public void requestsHaveAuth() throws IOException {
        exDl.downloadExercises(exercises, zipDestination, "", ExerciseObserver.NOP);

        wireMockRule.verify(
                getRequestedFor(urlEqualTo("/ex1.zip"))
                        .withHeader("Authorization", equalTo("Basic cGlobGE6anV1aA==")));

        wireMockRule.verify(
                getRequestedFor(urlEqualTo("/ex2.zip"))
                        .withHeader("Authorization", equalTo("Basic cGlobGE6anV1aA==")));
    }

    @Test
    public void downloadedExercisesExists() throws IOException {
        exDl.downloadExercises(exercises, zipDestination, "", ExerciseObserver.NOP);
        File exercise1 = Paths.get("src", "test", "resources", "__files", "testfile.txt").toFile();
        assertTrue("Zipped file testfile.txt was not downloaded to the fs", exercise1.exists());
    }

    @Test
    public void downloadedExercisesHasContent() throws IOException {
        exDl.downloadExercises(exercises, zipDestination, "", ExerciseObserver.NOP);

        String fileContent =
                FileUtils.readFileToString(
                        Paths.get("src", "test", "resources", "__files", "testfile.txt").toFile());

        String testFileContent = "Testfile for DownloadExercisesTest \n";
        assertEquals(testFileContent, fileContent);
    }

    @Test
    public void doesntCallUnzipOnLockedExercise() throws IOException {
        TaskExecutor executor = Mockito.mock(TaskExecutor.class);
        verify(executor, times(0)).extractProject(any(Path.class), any(Path.class));
        exDl = new ExerciseDownloader(new UrlCommunicator(settings), null, executor);
        exercises.get(0).setLocked(true);
        exercises.get(1).setLocked(true);
        exercises.get(2).setLocked(true);
        exDl.downloadExercises(exercises, zipDestination, "", ExerciseObserver.NOP);
    }

    @Test
    public void downloadsCorrectAmount() throws IOException {
        TaskExecutor executor = Mockito.mock(TaskExecutor.class);
        Mockito.doNothing().when(executor).extractProject(any(Path.class), any(Path.class));
        exDl =
                new ExerciseDownloader(
                        new UrlCommunicator(settings), new TmcApi(settings), executor);
        final AtomicInteger counter = new AtomicInteger();
        exDl.downloadExercises(
                exercises,
                zipDestination,
                "",
                new ExerciseObserver() {
                    @Override
                    public void observe(Exercise exercise, boolean success) {
                        if (success) {
                            counter.incrementAndGet();
                        }
                    }
                });
        assertEquals(3, counter.get());
    }

    @Test
    public void downloadsModelSolution() throws IOException, TmcCoreException {
        TaskExecutor executor = Mockito.mock(TaskExecutor.class);
        Mockito.doNothing()
                .when(executor)
                .extractProject(any(Path.class), any(Path.class), anyBoolean());
        exDl =
                new ExerciseDownloader(
                        new UrlCommunicator(settings), new TmcApi(settings), executor);
        Path path = Paths.get("not", "really", "a", "path");
        exDl.downloadModelSolution(modelSolutionExample, path);
        verify(executor).extractProject(any(Path.class), eq(path), eq(true));
    }
}
