package fi.helsinki.cs.tmc.core.commands;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import fi.helsinki.cs.tmc.core.communication.TmcServerCommunicationTaskFactory;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.holders.TmcLangsHolder;
import fi.helsinki.cs.tmc.core.holders.TmcSettingsHolder;
import fi.helsinki.cs.tmc.langs.util.TaskExecutor;
import fi.helsinki.cs.tmc.langs.util.TaskExecutorImpl;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;


public class GetUpdatableExercisesTest {

    @Rule public TemporaryFolder testFolder = new TemporaryFolder();

    @Mock ProgressObserver mockObserver;
    @Mock TmcSettings settings;
    @Mock TmcServerCommunicationTaskFactory factory;
    @Mock Course mockCurrentCourse;
    @Mock Course mockRefreshedCourse;
    @Mock Exercise uptodateLocalExercise;
    @Mock Exercise uptodateRefreshedExercise;
    @Mock Exercise updateableLocalExercise;
    @Mock Exercise updateableRefreshedExercise;
    @Mock Exercise newRefreshedExercise; // TODO: ?

    private TaskExecutor langs;
    private Path arithFuncsTempDir;

    private Command<List<Exercise>> command;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        langs = spy(new TaskExecutorImpl());
        TmcSettingsHolder.set(settings);
        TmcLangsHolder.set(langs);
        arithFuncsTempDir = testFolder.getRoot().toPath().resolve("arith_funcs");
        command = new GetUpdatableExercises(mockObserver, factory);

        when(settings.getCurrentCourse()).thenReturn(Optional.of(mockCurrentCourse));

        doCallRealMethod().when(langs).extractProject(any(Path.class), any(Path.class));
    }

    @Test
    public void noExercisesDownloadedTest() throws Exception {
        when(factory.getFullCourseInfoTask(mockCurrentCourse))
                .thenReturn(
                        new Callable<Course>() {
                            @Override
                            public Course call() throws Exception {
                                return mockRefreshedCourse;
                            }
                        });

        when(mockRefreshedCourse.getExercises())
                .thenReturn(
                        ImmutableList.of(
                                uptodateRefreshedExercise,
                                updateableRefreshedExercise,
                                newRefreshedExercise));

        // Note, the order of any and specific matcher matters!
        when(uptodateLocalExercise.isSameExercise(any(Exercise.class))).thenReturn(false);
        when(uptodateLocalExercise.isSameExercise(uptodateRefreshedExercise)).thenReturn(true);
        when(updateableLocalExercise.isSameExercise(any(Exercise.class))).thenReturn(false);
        when(updateableLocalExercise.isSameExercise(updateableRefreshedExercise)).thenReturn(true);
        when(newRefreshedExercise.isSameExercise(any(Exercise.class))).thenReturn(false);
        when(uptodateLocalExercise.getChecksum()).thenReturn("a1");
        when(uptodateRefreshedExercise.getChecksum()).thenReturn("a1");
        when(updateableLocalExercise.getChecksum()).thenReturn("a2");
        when(updateableRefreshedExercise.getChecksum()).thenReturn("b2");
        when(newRefreshedExercise.getChecksum()).thenReturn("c1");
        when(mockCurrentCourse.getExercises())
                .thenReturn(ImmutableList.of(uptodateLocalExercise, updateableLocalExercise));

        assertTrue(uptodateLocalExercise.isSameExercise(uptodateRefreshedExercise));
        assertFalse(uptodateLocalExercise.isSameExercise(updateableRefreshedExercise));
        assertTrue(updateableLocalExercise.isSameExercise(updateableRefreshedExercise));
        List<Exercise> updateableExercises = command.call();

        assertThat(updateableExercises).contains(updateableRefreshedExercise);
        assertThat(updateableExercises).hasSize(1);
        // TODO: when new exercise behaviour is fixed, remove two above and uncomment two below.
        //assertThat(updateableExercises)
        //.containsAllOf(updateableRefreshedExercise, newRefreshedExercise);
    }
}
