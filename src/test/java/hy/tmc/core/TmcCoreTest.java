package hy.tmc.core;

import com.google.common.util.concurrent.ListenableFuture;
import hy.tmc.core.testhelpers.ClientTmcSettings;
import com.google.common.util.concurrent.ListeningExecutorService;
import hy.tmc.core.commands.VerifyCredentials;
import hy.tmc.core.commands.DownloadExercises;
import hy.tmc.core.commands.GetExerciseUpdates;
import hy.tmc.core.commands.GetUnreadReviews;
import hy.tmc.core.commands.ListCourses;
import hy.tmc.core.commands.ListExercises;
import hy.tmc.core.commands.Paste;
import hy.tmc.core.commands.RunTests;
import hy.tmc.core.commands.SendFeedback;
import hy.tmc.core.commands.Submit;
import hy.tmc.core.domain.Course;
import hy.tmc.core.exceptions.TmcCoreException;
import hy.tmc.core.testhelpers.FileWriterHelper;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;

@RunWith(PowerMockRunner.class)
public class TmcCoreTest {

    private TmcCore tmcCore;
    private ListeningExecutorService threadPool;
    private Course course;
    private String tmcServerAddress = "https://tmc.mooc.fi/mooc";
    private ClientTmcSettings settings;

    @Before
    public void setUp() throws IOException {
        threadPool = mock(ListeningExecutorService.class);
        tmcCore = new TmcCore(threadPool);
        course = new Course();
        settings = new ClientTmcSettings("test", "1234");
        settings.setServerAddress(tmcServerAddress);
        Paths.get("src", "test", "resources", "cachefile").toFile().createNewFile();
        Paths.get("src", "test", "resources", "file2.cache").toFile().createNewFile();
    }
    
    @After
    public void cleanUp() {
        Paths.get("src", "test", "resources", "cachefile").toFile().delete();
        Paths.get("src", "test", "resources", "file2.cache").toFile().delete();
    }

    @Test
    public void verifyCredentials() throws Exception {
        tmcCore.verifyCredentials(settings);
        verify(threadPool, times(1)).submit(any(VerifyCredentials.class));
    }

    @Test(expected = TmcCoreException.class)
    public void loginWithoutNumberFails() throws Exception {
        ClientTmcSettings emptySettings = new ClientTmcSettings("", "");
        emptySettings.setServerAddress(tmcServerAddress);
        tmcCore.verifyCredentials(emptySettings).get();
    }
    
    @Test(expected = TmcCoreException.class)
    public void loginWithoutServerAddrees() throws Exception {
        ClientTmcSettings emptySettings = new ClientTmcSettings("", "");
        emptySettings.setServerAddress("");
        tmcCore.verifyCredentials(emptySettings).get();
    }

    @Test
    public void downloadExercises() throws Exception {
        tmcCore.downloadExercises("/polku/tiedostoille", "21", settings);
        verify(threadPool, times(1)).submit(any(DownloadExercises.class));
    }

    @Test
    public void listCourses() throws Exception {
        tmcCore.listCourses(settings);
        verify(threadPool, times(1)).submit(any(ListCourses.class));
    }

    @Test
    public void listExercises() throws Exception {
        tmcCore.listExercises("path/kurssiin", settings);
        verify(threadPool, times(1)).submit(any(ListExercises.class));
    }

    @Test(expected = FileNotFoundException.class)
    public void nonExistantCacheFileThrowsException() throws Exception {
        Path path = Paths.get("src", "test", "resources", "nothere.cache");
        tmcCore.setCacheFile(path.toFile());
        tmcCore.getNewAndUpdatedExercises(course, settings);
    }

    @Test(expected = FileNotFoundException.class)
    public void nonExistantCacheFileThrowsExceptionFromConstructor() throws Exception {
        Path path = Paths.get("src", "test", "resources", "nothere.cache");
        new TmcCore(path.toFile(), threadPool);
    }

    @Test
    public void getExerciseUpdatesTest() throws Exception {
        Path path = Paths.get("src", "test", "resources", "cachefile");
        tmcCore.setCacheFile(path.toFile());
        tmcCore.getNewAndUpdatedExercises(course, settings);
        verify(threadPool, times(1)).submit(any(GetExerciseUpdates.class));
        assertEquals(tmcCore.getCacheFile(), path.toFile());
    }

    @Test
    public void withNoCacheFileErrorIsThrownWithExerciseUpdates() throws Exception {
        // using catch to verify command has not been sent
        try {
            tmcCore.getNewAndUpdatedExercises(course, settings);
        }
        catch (TmcCoreException ex) {
            verify(threadPool, times(0)).submit(any(GetExerciseUpdates.class));
            return;
        }
        fail("expected TmcCoreException");

    }

    @Test(expected = FileNotFoundException.class)
    public void nullCaughtTest() throws FileNotFoundException {
        new TmcCore((File) null);
    }

    @Test(expected = FileNotFoundException.class)
    public void nullCaughtInSetTest() throws Exception {
        tmcCore.setCacheFile(null);
    }

    @Test(expected = FileNotFoundException.class)
    public void nonExistentFileInSetTest() throws Exception {
        File fake = Paths.get("src", "test", "resources", "fakeFile.cache").toFile();
        tmcCore.setCacheFile(fake);
    }

    @Test
    public void cacheFileGivenInConstructorTest() throws Exception {
        Path path = Paths.get("src", "test", "resources", "cachefile");
        TmcCore core = new TmcCore(path.toFile(), threadPool);
        core.getNewAndUpdatedExercises(course, settings);
        assertEquals(core.getCacheFile(), path.toFile());
        verify(threadPool, times(1)).submit(any(GetExerciseUpdates.class));
    }

    @Test
    public void migratingCacheFileKeepsOldCacheData() throws Exception {
        Path firstPath = Paths.get("src", "test", "resources", "cachefile");
        Path secondPath = Paths.get("src", "test", "resources", "file2.cache");
        tmcCore.setCacheFile(firstPath.toFile());
        new FileWriterHelper().writeStuffToFile(firstPath.toString());
        tmcCore.setCacheFile(secondPath.toFile());
        assertFalse(FileUtils.readFileToString(secondPath.toFile()).isEmpty());
        assertFalse(firstPath.toFile().exists());
        assertEquals(tmcCore.getCacheFile(), secondPath.toFile());
    }

    @Test
    public void getNewReviewsTest() throws TmcCoreException {
        tmcCore.getNewReviews(course, settings);
        verify(threadPool, times(1)).submit(any(GetUnreadReviews.class));
    }

    @Test
    public void test() throws Exception {
        tmcCore.test("testi/polku", settings);
        verify(threadPool, times(1)).submit(any(RunTests.class));
    }

    @Test
    public void sendFeedback() throws Exception {
        tmcCore.sendFeedback(new HashMap<String, String>(), "internet.computer/file", settings);
        verify(threadPool, times(1)).submit(any(SendFeedback.class));
    }

    @Test
    public void submit() throws Exception {
        tmcCore.submit("polku/tiedostoon", settings);
        verify(threadPool, times(1)).submit(any(Submit.class));
    }

    @Test
    public void pasteTest() throws Exception {
        tmcCore.paste("polku/jonnekin", settings);
        verify(threadPool, times(1)).submit(any(Paste.class));
    }

    @Test(expected = TmcCoreException.class)
    public void submitWithBadPathThrowsException() throws TmcCoreException {
        tmcCore.submit("", settings);
    }

    @Test(expected = TmcCoreException.class)
    public void downloadExercisesWithBadPathThrowsException() throws Exception {
        tmcCore.downloadExercises(null, "2", settings);
    }

    @Test
    public void downloadExercisesUsesCacheIfSet() throws Exception {
        tmcCore.setCacheFile(Paths.get("src", "test", "resources", "cachefile").toFile());
        tmcCore.downloadExercises("asdf", "asdf", settings);
        final ArgumentCaptor<DownloadExercises> argument = ArgumentCaptor.forClass(DownloadExercises.class);
        verify(threadPool).submit(argument.capture());
        assertTrue(argument.getValue().cacheFileSet());
    }
    
    @Test
    public void downloadExercisesDoesNotUseCacheIfNotSet() throws Exception {
        tmcCore.downloadExercises("asdf", "asdf", settings);
        final ArgumentCaptor<DownloadExercises> argument = ArgumentCaptor.forClass(DownloadExercises.class);
        verify(threadPool).submit(argument.capture());
        assertFalse(argument.getValue().cacheFileSet());
    }
}
