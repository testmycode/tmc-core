package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.CoreTestSettings;
import fi.helsinki.cs.tmc.core.communication.ExerciseDownloader;
import fi.helsinki.cs.tmc.core.communication.TmcApi;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import java.nio.file.Paths;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DownloadModelSolutionTest {

    private DownloadModelSolution dl;
    private Exercise exercise;
    private CoreTestSettings settings;
    private TmcApi api;
    private ExerciseDownloader downloader;

    public DownloadModelSolutionTest() {
        exercise = new Exercise("tehtava");
        exercise.setCourseName("tira");
        settings = new CoreTestSettings();
        settings.setTmcMainDirectory("home");
        settings.setCurrentCourse(new Course("ohpe"));
        settings.setCredentials("a", "b");
    }

    @Before
    public void setUp() {
        downloader = mock(ExerciseDownloader.class);
        dl = new DownloadModelSolution(settings, exercise, downloader);
        when(downloader.createCourseFolder(anyString(), anyString())).thenReturn("path");
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testCall() throws Exception {
        dl.call();
        verify(downloader).createCourseFolder(eq(exercise.getName()), eq("tira"));
        verify(downloader).downloadModelSolution(eq(exercise), eq(Paths.get("path")));
    }

    @Test
    public void testFallbackToCourseInSettings() throws Exception {
        exercise.setCourseName("");
        dl.call();
        verify(downloader).createCourseFolder(eq(exercise.getName()), eq("ohpe"));
        verify(downloader).downloadModelSolution(eq(exercise), eq(Paths.get("path")));
    }

}
