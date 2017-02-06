package fi.helsinki.cs.tmc.core.commands;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

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

import com.google.common.collect.Lists;

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

public class DownloadOrUpdateExercisesTest {

    @Rule public TemporaryFolder testFolder = new TemporaryFolder();

    @Mock ProgressObserver mockObserver;
    @Spy TmcSettings settings = new MockSettings();
    @Mock TmcServerCommunicationTaskFactory factory;
    @Mock Course mockCourse;
    @Mock Exercise mockExerciseOne;

    TaskExecutor langs;
    Path arithFuncsTempDir;

    private Command<List<Exercise>> command;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        langs = spy(new TaskExecutorImpl());
        TmcSettingsHolder.set(settings);
        TmcLangsHolder.set(langs);
        arithFuncsTempDir = testFolder.getRoot().toPath().resolve("arith_funcs");
        List<Exercise> exList = Lists.newArrayList(mockExerciseOne);
        command = new DownloadOrUpdateExercises(mockObserver, exList, factory);
        mockExerciseOne.setName("arith_funcs");
        mockExerciseOne.setCourseName("test_course");

        doCallRealMethod().when(langs).extractProject(any(Path.class), any(Path.class));
    }

    @Test
    public void testSuccess() throws Exception {
        verifyZeroInteractions(langs);

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

        List<Exercise> exercises = command.call();

        verify(factory).getDownloadingExerciseZipTask(mockExerciseOne);
        verifyNoMoreInteractions(factory);

        assertThat(exercises).contains(mockExerciseOne);
        assertThat(exercises).hasSize(1);
        assertTrue(Files.exists(arithFuncsTempDir));
        // TODO: check for contents?
    }

    @Test
    public void testNoFileIsWrittenWhenDownloadFails() throws Exception {
        verifyZeroInteractions(langs);
        when(factory.getDownloadingExerciseZipTask(mockExerciseOne))
                .thenThrow(new RuntimeException("fail"));

        List<Exercise> exercises = command.call();

        verify(factory).getDownloadingExerciseZipTask(mockExerciseOne);
        verifyNoMoreInteractions(factory);

        assertThat(exercises).isEmpty();
        assertFalse(Files.exists(arithFuncsTempDir));
    }
}
