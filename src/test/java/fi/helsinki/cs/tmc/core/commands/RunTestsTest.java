package fi.helsinki.cs.tmc.core.commands;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.holders.TmcLangsHolder;
import fi.helsinki.cs.tmc.core.holders.TmcSettingsHolder;
import fi.helsinki.cs.tmc.core.utils.TestUtils;
import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.domain.RunResult.Status;
import fi.helsinki.cs.tmc.langs.util.TaskExecutor;
import fi.helsinki.cs.tmc.langs.util.TaskExecutorImpl;
import java.nio.file.Path;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class RunTestsTest {

    @Rule public TemporaryFolder testFolder = new TemporaryFolder();
    Path project;

    @Mock ProgressObserver mockObserver;
    @Mock TmcSettings settings;
    @Mock Course mockCourse;
    @Mock Exercise mockExercise;

    TaskExecutor langs;
    Path arithFuncsTempDir;

    private Command<RunResult> command;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        TmcSettingsHolder.set(settings);
        langs = spy(new TaskExecutorImpl());
        TmcLangsHolder.set(langs);
        project = TestUtils.getProject(RunTestsTest.class, "arith_funcs");

        command = new RunTests(mockObserver, mockExercise);
        when(settings.getTmcProjectDirectory()).thenReturn(testFolder.getRoot().toPath());
        when(mockExercise.getExerciseDirectory(any(Path.class))).thenReturn(project);
        doCallRealMethod().when(langs).runTests(project);
    }

    @Test
    public void testSuccess() throws Exception {
        RunResult result = command.call();
        assertEquals(result.status, Status.PASSED);
    }
}
