package fi.helsinki.cs.tmc.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import fi.helsinki.cs.tmc.core.commands.DownloadExercises;
import fi.helsinki.cs.tmc.core.commands.GetExerciseUpdates;
import fi.helsinki.cs.tmc.core.commands.GetUnreadReviews;
import fi.helsinki.cs.tmc.core.commands.ListCourses;
import fi.helsinki.cs.tmc.core.commands.RunTests;
import fi.helsinki.cs.tmc.core.commands.SendFeedback;
import fi.helsinki.cs.tmc.core.commands.Submit;
import fi.helsinki.cs.tmc.core.commands.VerifyCredentials;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;
import fi.helsinki.cs.tmc.core.testhelpers.FileWriterHelper;

import com.google.common.util.concurrent.ListeningExecutorService;
import org.apache.commons.io.FileUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class TmcCoreTest {

    private TmcCore tmcCore;
    private ListeningExecutorService threadPool;
    private Course course;
    private String tmcServerAddress = "https://tmc.mooc.fi/mooc";
    private CoreTestSettings settings;

    @Before
    public void setUp() throws IOException {
        threadPool = Mockito.mock(ListeningExecutorService.class);
        tmcCore = new TmcCore(threadPool);
        course = new Course();
        settings = new CoreTestSettings("test", "1234");
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
        CoreTestSettings emptySettings = new CoreTestSettings("", "");
        emptySettings.setServerAddress(tmcServerAddress);
        tmcCore.verifyCredentials(emptySettings).get();
    }

    @Test(expected = TmcCoreException.class)
    public void loginWithoutServerAddrees() throws Exception {
        CoreTestSettings emptySettings = new CoreTestSettings("", "");
        emptySettings.setServerAddress("");
        tmcCore.verifyCredentials(emptySettings).get();
    }

    @Test
    public void downloadExercises() throws Exception {
        tmcCore.downloadExercises("/polku/tiedostoille", "21", settings, null);
        verify(threadPool, times(1)).submit(any(DownloadExercises.class));
    }

    @Test
    public void listCourses() throws Exception {
        tmcCore.listCourses(settings);
        verify(threadPool, times(1)).submit(any(ListCourses.class));
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
        } catch (TmcCoreException ex) {
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

    /*@Test
     public void pasteTest() throws Exception {
     tmcCore.pasteWithComment("polku/jonnekin", settings, "");
     verify(threadPool, times(1)).submit(any(PasteWithComment.class));
     }*/

    @Test(expected = TmcCoreException.class)
    public void submitWithBadPathThrowsException() throws TmcCoreException {
        tmcCore.submit("", settings);
    }

    @Test(expected = TmcCoreException.class)
    public void downloadExercisesWithBadPathThrowsException() throws Exception {
        tmcCore.downloadExercises(null, "2", settings, null);
    }

    @Test
    public void downloadExercisesUsesCacheIfSet() throws Exception {
        tmcCore.setCacheFile(Paths.get("src", "test", "resources", "cachefile").toFile());
        tmcCore.downloadExercises("asdf", "asdf", settings, null);
        final ArgumentCaptor<DownloadExercises> argument =
                ArgumentCaptor.forClass(DownloadExercises.class);
        verify(threadPool).submit(argument.capture());
        assertTrue(argument.getValue().cacheFileSet());
    }

    @Test
    public void downloadExercisesDoesNotUseCacheIfNotSet() throws Exception {
        tmcCore.downloadExercises("asdf", "asdf", settings, null);
        final ArgumentCaptor<DownloadExercises> argument =
                ArgumentCaptor.forClass(DownloadExercises.class);
        verify(threadPool).submit(argument.capture());
        assertFalse(argument.getValue().cacheFileSet());
    }
}
