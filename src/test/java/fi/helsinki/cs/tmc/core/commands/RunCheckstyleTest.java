package fi.helsinki.cs.tmc.core.commands;

import static com.google.common.truth.Truth.assertThat;
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
import fi.helsinki.cs.tmc.core.utils.MockSettings;
import fi.helsinki.cs.tmc.core.utils.TestUtils;
import fi.helsinki.cs.tmc.langs.abstraction.ValidationResult;
import fi.helsinki.cs.tmc.langs.util.TaskExecutor;
import fi.helsinki.cs.tmc.langs.util.TaskExecutorImpl;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.nio.file.Path;
import java.util.Locale;

public class RunCheckstyleTest {

    @Rule public TemporaryFolder testFolder = new TemporaryFolder();

    @Mock ProgressObserver mockObserver;
    @Spy TmcSettings settings = new MockSettings();
    @Mock Course mockCourse;
    @Mock Exercise mockExercise;

    private Path project;
    private TaskExecutor langs;
    private Path arithFuncsTempDir;
    private Command<ValidationResult> command;

    private static final Locale LOCALE = new Locale("FI");

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        TmcSettingsHolder.set(settings);
        langs = spy(new TaskExecutorImpl());
        TmcLangsHolder.set(langs);
        project = TestUtils.getProject(RunCheckstyleTest.class, "arith_funcs");

        command = new RunCheckStyle(mockObserver, mockExercise);
        when(settings.getTmcProjectDirectory()).thenReturn(testFolder.getRoot().toPath());
        when(mockExercise.getExerciseDirectory(any(Path.class))).thenReturn(project);
        doCallRealMethod().when(langs).runCheckCodeStyle(any(Path.class), any(Locale.class));
        when(settings.getLocale()).thenReturn(LOCALE);
    }

    @Test
    public void testSuccess() throws Exception {
        ValidationResult result = command.call();
        assertThat(result.getValidationErrors()).isEmpty();
    }
}
