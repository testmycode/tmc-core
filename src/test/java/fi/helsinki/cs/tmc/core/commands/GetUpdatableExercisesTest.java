package fi.helsinki.cs.tmc.core.commands;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
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
import fi.helsinki.cs.tmc.core.utils.MockSettings;
import fi.helsinki.cs.tmc.langs.util.TaskExecutor;
import fi.helsinki.cs.tmc.langs.util.TaskExecutorImpl;

import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.nio.file.Path;
import java.util.concurrent.Callable;

public class GetUpdatableExercisesTest {

    @Rule public TemporaryFolder testFolder = new TemporaryFolder();

    @Mock ProgressObserver mockObserver;
    @Spy TmcSettings settings = new MockSettings();
    @Mock TmcServerCommunicationTaskFactory factory;
    @Mock Course mockCurrentCourse;
    @Mock Course mockRefreshedCourse;
    @Mock Exercise uptodateLocalExercise;
    @Mock Exercise uptodateRefreshedExercise;
    @Mock Exercise updateableLocalExercise;
    @Mock Exercise updateableRefreshedExercise;
    @Mock Exercise deletableLocalExercise;
    @Mock Exercise newRefreshedExercise;

    private TaskExecutor langs;
    private Path arithFuncsTempDir;

    private Command<GetUpdatableExercises.UpdateResult> command;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        langs = spy(new TaskExecutorImpl());
        TmcSettingsHolder.set(settings);
        TmcLangsHolder.set(langs);
        arithFuncsTempDir = testFolder.getRoot().toPath().resolve("arith_funcs");
        command = new GetUpdatableExercises(mockObserver, factory, mockCurrentCourse);

        doCallRealMethod().when(langs).extractProject(any(Path.class), any(Path.class));
    }

    @Test
    public void updatedExercisesAreRecognicedTest() throws Exception {
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
        when(uptodateLocalExercise.getName()).thenReturn("e1");
        when(uptodateRefreshedExercise.getName()).thenReturn("e1");
        when(updateableLocalExercise.getName()).thenReturn("e2");
        when(updateableRefreshedExercise.getName()).thenReturn("e2");
        when(newRefreshedExercise.getName()).thenReturn("e3");
        when(uptodateLocalExercise.getChecksum()).thenReturn("a1");
        when(uptodateRefreshedExercise.getChecksum()).thenReturn("a1");
        when(updateableLocalExercise.getChecksum()).thenReturn("a2");
        when(updateableRefreshedExercise.getChecksum()).thenReturn("b2");
        when(newRefreshedExercise.getChecksum()).thenReturn("c1");
        when(mockCurrentCourse.getExercises())
                .thenReturn(ImmutableList.of(uptodateLocalExercise, updateableLocalExercise));

        assertEquals(uptodateLocalExercise.getName(), uptodateRefreshedExercise.getName());
        assertNotEquals(uptodateLocalExercise.getName(), updateableRefreshedExercise.getName());
        assertEquals(updateableLocalExercise.getName(), updateableRefreshedExercise.getName());
        GetUpdatableExercises.UpdateResult updateResults = command.call();

        assertThat(updateResults.getNewExercises()).containsExactly(newRefreshedExercise);
        assertThat(updateResults.getUpdatedExercises())
                .containsExactly(updateableRefreshedExercise);
    }

    @Test
    public void ignoreDeletedExercisesTest() throws Exception {
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
                                uptodateRefreshedExercise));

        // Note, the order of any and specific matcher matters!
        when(uptodateLocalExercise.getName()).thenReturn("e1");
        when(uptodateRefreshedExercise.getName()).thenReturn("e1");
        when(deletableLocalExercise.getName()).thenReturn("e2");

        when(uptodateLocalExercise.getChecksum()).thenReturn("a1");
        when(uptodateRefreshedExercise.getChecksum()).thenReturn("a1");
        when(deletableLocalExercise.getChecksum()).thenReturn("a2");
        when(mockCurrentCourse.getExercises())
                .thenReturn(ImmutableList.of(uptodateLocalExercise, deletableLocalExercise));

        assertEquals(uptodateLocalExercise.getName(), uptodateRefreshedExercise.getName());
        assertNotEquals(uptodateLocalExercise.getName(), updateableRefreshedExercise.getName());
        assertEquals(updateableLocalExercise.getName(), updateableRefreshedExercise.getName());
        GetUpdatableExercises.UpdateResult updateResults = command.call();

        assertThat(updateResults.getNewExercises()).isEmpty();
        assertThat(updateResults.getUpdatedExercises()).isEmpty();
    }
}
