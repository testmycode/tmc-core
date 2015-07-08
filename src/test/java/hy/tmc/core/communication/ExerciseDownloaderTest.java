package hy.tmc.core.communication;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertEquals;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.base.Optional;
import hy.tmc.core.testhelpers.ClientTmcSettings;
import hy.tmc.core.domain.Exercise;
import hy.tmc.core.exceptions.TmcCoreException;
import hy.tmc.core.zipping.DefaultUnzipDecider;
import hy.tmc.core.zipping.Unzipper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ExerciseDownloaderTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();

    private ArrayList<Exercise> exercises;
    private ExerciseDownloader exDl;
    private ClientTmcSettings settings;
    private Unzipper zipHandler;
    private String testFileContent = "Testfile for DownloadExercisesTest \n";
    private String testZipPath;
    private String contentFilePath;
    private String zipDestination;

    /**
     * Creates required stubs and example data for downloader.
     */
    @Before
    public void setup() {
        settings = new ClientTmcSettings();
        zipHandler = Mockito.mock(Unzipper.class);

        exDl = new ExerciseDownloader(
                new DefaultUnzipDecider(),
                new UrlCommunicator(settings),
                new TmcJsonParser(settings)

        );
        exercises = new ArrayList<>();

        testZipPath = "testzip.zip";
        contentFilePath = "testfile.txt";
        zipDestination = Paths.get("src", "test", "resources", "__files").toString();

        Exercise e1 = new Exercise();
        e1.setZipUrl("http://127.0.0.1:8080/ex1.zip");
        e1.setName("Exercise1");
        exercises.add(e1);

        Exercise e2 = new Exercise();
        e2.setZipUrl("http://127.0.0.1:8080/ex2.zip");
        e2.setName("Exercise2");
        exercises.add(e2);

        Exercise e3 = new Exercise();
        e3.setZipUrl("http://127.0.0.1:8080/ex3.zip");
        e3.setName("Exercise3");
        exercises.add(e3);

        stubFor(get(urlEqualTo("/ex1.zip"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/xml")
                        .withBodyFile(testZipPath)));

        wireMockRule.stubFor(get(urlEqualTo("/ex2.zip"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/xml")
                        .withBody("<response>Exercise 2</response>")));
        
        wireMockRule.stubFor(get(urlEqualTo("/ex3.zip"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/xml")
                        .withBody("<response>Exercise 3</response>")));

        stubFor(get(urlEqualTo("/emptyCourse.json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"api_version\":7,\"course\":{\"id\":21,\"name\":\"k2015-tira\","
                                + "\"details_url\":\"https://tmc.mooc.fi/staging/courses/21.json\""
                                + ",\"unlock_url\":\"https://tmc.mooc.fi/staging/courses/21/unlock"
                                + ".json\",\"reviews_url\":\"https://tmc.mooc.fi/staging/courses"
                                + "/21/reviews"
                                + ".json\",\"comet_url\":\"https://tmc.mooc.fi:8443/"
                                + "comet\",\"spyware_urls\":[\"http://staging.spyware."
                                + "testmycode.net/\"],\"unlockables\":[],\"exercises\":[]}}")));


        settings.setUsername("pihla");
        settings.setPassword("juuh");
    }

    @After
    public void remove() {
        Paths.get("src", "test", "resources", "__files", "testfile.txt").toFile().delete();
    }

    @Test
    public void downloadExercisesDoesRequests() {
        exDl.downloadFiles(exercises, zipDestination);
        wireMockRule.verify(getRequestedFor(urlEqualTo("/ex1.zip")));
        wireMockRule.verify(getRequestedFor(urlEqualTo("/ex2.zip")));
    }

    @Test
    public void requestsHaveAuth() {
        exDl.downloadFiles(exercises, zipDestination);

        wireMockRule.verify(getRequestedFor(urlEqualTo("/ex1.zip"))
                .withHeader("Authorization", equalTo("Basic cGlobGE6anV1aA==")));

        wireMockRule.verify(getRequestedFor(urlEqualTo("/ex2.zip"))
                .withHeader("Authorization", equalTo("Basic cGlobGE6anV1aA==")));
    }

    @Test
    public void exerciseListIsEmpty() throws IOException, TmcCoreException {
        Optional<List<Exercise>> exercises = exDl.downloadExercises("http://127.0.0.1:8080/emptyCourse.json");
        List<Exercise> list = exercises.or(new ArrayList<Exercise>());
        assertEquals(0, list.size());
    }

    @Test
    public void downloadedExercisesExists() {
        exDl.downloadFiles(exercises, zipDestination);
        File exercise1 = Paths.get("src", "test", "resources", "__files", "testfile.txt").toFile();
        assertTrue("Zipped file testfile.txt was not downloaded to the fs", exercise1.exists());
        //File exercise2 = new File("Exercise2.zip");
        //assertTrue("File Exercise2 was not downloaded to the fs", exercise2.exists());
    }

    @Test
    public void downloadedExercisesHasContent() throws IOException {
        exDl.downloadFiles(exercises, zipDestination);


        String fileContent = FileUtils.readFileToString(
                Paths.get("src", "test", "resources", "__files", "testfile.txt").toFile()
        );

        assertEquals(this.testFileContent, fileContent);
    }

    @Test
    public void doesntCallUnzipOnLockedExercise() {
        DefaultUnzipDecider mockedDecider = mock(DefaultUnzipDecider.class);
        exDl = new ExerciseDownloader(mockedDecider, new UrlCommunicator(settings), null);
        exercises.get(0).setLocked(true);
        exercises.get(1).setLocked(true);
        exDl.downloadFiles(exercises, zipDestination);
        

        verify(mockedDecider, times(0)).canBeOverwritten(anyString());
        verify(mockedDecider, times(0)).readTmcprojectYml(any(Path.class));
    }

    @Test
    public void downloadsCorrectAmount() throws IOException, ZipException {
        exDl = new ExerciseDownloader(
                new DefaultUnzipDecider(),
                new UrlCommunicator(settings),
                new TmcJsonParser(settings), 
                zipHandler
        );
        Mockito.doNothing().when(zipHandler).unzip();
        Optional<List<Exercise>> optionalList = exDl.downloadFiles(exercises, zipDestination);
        List<Exercise> list = optionalList.or(new ArrayList<Exercise>());
        assertEquals(3, list.size());
    }
}
