package hy.tmc.core;

import com.google.common.util.concurrent.ListeningExecutorService;
import hy.tmc.core.commands.Authenticate;
import hy.tmc.core.commands.ChooseServer;
import hy.tmc.core.commands.DownloadExercises;
import hy.tmc.core.commands.GetExerciseUpdates;
import hy.tmc.core.commands.GetUnreadReviews;
import hy.tmc.core.commands.ListCourses;
import hy.tmc.core.commands.ListExercises;
import hy.tmc.core.commands.Logout;
import hy.tmc.core.commands.Paste;
import hy.tmc.core.commands.RunTests;
import hy.tmc.core.commands.SendFeedback;
import hy.tmc.core.commands.Submit;
import hy.tmc.core.domain.Course;
import hy.tmc.core.domain.Credentials;
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

    @Before
    public void setUp() throws IOException {
        threadPool = mock(ListeningExecutorService.class);
        tmcCore = new TmcCore(threadPool);
        course = new Course();
        Paths.get("src", "test", "resources", "cachefile").toFile().createNewFile();
        Paths.get("src", "test", "resources", "file2.cache").toFile().createNewFile();
    }
    
    @After
    public void cleanUp() {
        Paths.get("src", "test", "resources", "cachefile").toFile().delete();
        Paths.get("src", "test", "resources", "file2.cache").toFile().delete();
    }

    @Test
    public void login() throws Exception {
        tmcCore.login(new Credentials("test", "1234"), tmcServerAddress);
        verify(threadPool, times(1)).submit(any(Authenticate.class));
    }

    @Test(expected = TmcCoreException.class)
    public void loginWithoutNumberFails() throws Exception {
        tmcCore.login(new Credentials("",""), tmcServerAddress).get();
    }
    
    @Test(expected = TmcCoreException.class)
    public void loginWithoutServerAddrees() throws Exception {
        tmcCore.login(new Credentials("test","1234"), "").get();
    }


    @Test
    public void logout() throws Exception {
        tmcCore.logout();
        verify(threadPool, times(1)).submit(any(Logout.class));
    }

    @Test
    public void selectServer() throws Exception {
        tmcCore.selectServer("uusiServu");
        verify(threadPool, times(1)).submit(any(ChooseServer.class));
    }

    @Test
    public void downloadExercises() throws Exception {
        tmcCore.downloadExercises("/polku/tiedostoille", "21");
        verify(threadPool, times(1)).submit(any(DownloadExercises.class));
    }

    @Test
    public void listCourses() throws Exception {
        tmcCore.listCourses();
        verify(threadPool, times(1)).submit(any(ListCourses.class));
    }

    @Test
    public void listExercises() throws Exception {
        tmcCore.listExercises("path/kurssiin");
        verify(threadPool, times(1)).submit(any(ListExercises.class));
    }

    @Test(expected = FileNotFoundException.class)
    public void nonExistantCacheFileThrowsException() throws Exception {
        Path path = Paths.get("src", "test", "resources", "nothere.cache");
        tmcCore.setCacheFile(path.toFile());
        tmcCore.getNewAndUpdatedExercises(course);
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
        tmcCore.getNewAndUpdatedExercises(course);
        verify(threadPool, times(1)).submit(any(GetExerciseUpdates.class));
        assertEquals(tmcCore.getCacheFile(), path.toFile());
    }

    @Test
    public void withNoCacheFileErrorIsThrownWithExerciseUpdates() throws Exception {
        // using catch to verify command has not been sent
        try {
            tmcCore.getNewAndUpdatedExercises(course);
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
        core.getNewAndUpdatedExercises(course);
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
        tmcCore.getNewReviews(course);
        verify(threadPool, times(1)).submit(any(GetUnreadReviews.class));
    }

    @Test
    public void test() throws Exception {
        tmcCore.test("testi/polku");
        verify(threadPool, times(1)).submit(any(RunTests.class));
    }

    @Test
    public void sendFeedback() throws Exception {
        tmcCore.sendFeedback(new HashMap<String, String>(), "internet.computer/file");
        verify(threadPool, times(1)).submit(any(SendFeedback.class));
    }

    @Test
    public void submit() throws Exception {
        tmcCore.submit("polku/tiedostoon");
        verify(threadPool, times(1)).submit(any(Submit.class));
    }

    @Test
    public void pasteTest() throws Exception {
        tmcCore.paste("polku/jonnekin");
        verify(threadPool, times(1)).submit(any(Paste.class));
    }

    @Test(expected = TmcCoreException.class)
    public void submitWithBadPathThrowsException() throws TmcCoreException {
        tmcCore.submit("");
    }

    @Test(expected = TmcCoreException.class)
    public void downloadExercisesWithBadPathThrowsException() throws Exception {
        tmcCore.downloadExercises(null, "2");
    }

    @Test
    public void downloadExercisesUsesCacheIfSet() throws Exception {
        tmcCore.setCacheFile(Paths.get("src", "test", "resources", "cachefile").toFile());
        tmcCore.downloadExercises("asdf", "asdf");
        final ArgumentCaptor<DownloadExercises> argument = ArgumentCaptor.forClass(DownloadExercises.class);
        verify(threadPool).submit(argument.capture());
        assertTrue(argument.getValue().cacheFileSet());
    }
    
    @Test
    public void downloadExercisesDoesNotUseCacheIfNotSet() throws Exception {
        tmcCore.downloadExercises("asdf", "asdf");
        final ArgumentCaptor<DownloadExercises> argument = ArgumentCaptor.forClass(DownloadExercises.class);
        verify(threadPool).submit(argument.capture());
        assertFalse(argument.getValue().cacheFileSet());
    }

}
