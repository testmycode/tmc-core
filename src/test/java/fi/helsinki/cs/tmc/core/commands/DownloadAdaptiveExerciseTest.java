package fi.helsinki.cs.tmc.core.commands;

import com.google.common.collect.Lists;
import fi.helsinki.cs.tmc.core.communication.TmcServerCommunicationTaskFactory;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.holders.TmcLangsHolder;
import fi.helsinki.cs.tmc.core.holders.TmcSettingsHolder;
import fi.helsinki.cs.tmc.core.utils.MockSettings;
import fi.helsinki.cs.tmc.core.utils.TestUtils;
import fi.helsinki.cs.tmc.langs.util.TaskExecutor;
import fi.helsinki.cs.tmc.langs.util.TaskExecutorImpl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by markovai on 18.5.2017.
 */
public class DownloadAdaptiveExerciseTest {

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Mock
    ProgressObserver mockObserver;
    @Spy
    TmcSettings settings = new MockSettings();
    @Mock
    TmcServerCommunicationTaskFactory factory;
    @Mock
    Course mockCourse;
    @Mock
    Exercise mockExerciseOne;
    @Mock
    Callable<Exercise> mockGetAdaptiveExercise;

    TaskExecutor langs;
    Path arithFuncsTempDir;

    private Command<Exercise> command;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        langs = spy(new TaskExecutorImpl());
        TmcSettingsHolder.set(settings);
        TmcLangsHolder.set(langs);
        arithFuncsTempDir = testFolder.getRoot().toPath().resolve("arith_funcs");
        command = new DownloadAdaptiveExercise(mockObserver, factory);

        doCallRealMethod().when(langs).extractProject(any(Path.class), any(Path.class));
        mockExerciseOne.setName("ex1");
        mockExerciseOne.setCourseName("course1");
    }

    @Test
    public void testSuccess() throws Exception {
        verifyZeroInteractions(langs);

        when(factory.getAdaptiveExercise()).thenReturn(mockGetAdaptiveExercise);
        when(mockGetAdaptiveExercise.call()).thenReturn(mockExerciseOne);

        when(mockExerciseOne.getExtractionTarget(any(Path.class))).thenReturn(arithFuncsTempDir);
        when(settings.getTmcProjectDirectory()).thenReturn(testFolder.getRoot().toPath());

        when(factory.getDownloadingExerciseZipTask(mockExerciseOne))
            .thenReturn(
                new Callable<byte[]>() {
                    @Override
                    public byte[] call() throws Exception {
                        return Files.readAllBytes(
                            TestUtils.getZip(this.getClass(), "arith_funcs.zip"));
                    }
                });

        Exercise exercise = command.call();

        verify(factory).getDownloadingExerciseZipTask(mockExerciseOne);
        //verifyNoMoreInteractions(factory);

        assertTrue(Files.exists(arithFuncsTempDir));
        // TODO: check for contents?
    }

}
