package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.TmcServerCommunicationTaskFactory;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.domain.submission.SubmissionResult;
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

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * Created by markovai on 23.5.2017.
 */
public class SubmitAdaptiveExerciseToSkillifierTest {
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
    Exercise mockExercise;

    private static final URI PASTE_URI = URI.create("http://example.com/paste");
    private static final URI SUBMISSION_URI = URI.create("http://example.com/submission");
    private static final TmcServerCommunicationTaskFactory.SubmissionResponse STUB_RESPONSE =
        new TmcServerCommunicationTaskFactory.SubmissionResponse(SUBMISSION_URI, PASTE_URI);

    private static final String STUB_PROSESSING_RESPONSE = "{status: \"processing\"}";
    private static final String STUB_PROSESSING_DONE_RESPONSE = "{status: \"OK\"}";

    private Command<SubmissionResult> command;
    private Path arithFuncsTempDir;
    private TaskExecutor langs;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        TmcSettingsHolder.set(settings);
        //when(settings.getTmcProjectDirectory()).thenReturn(Paths.get(System.getProperty("user.dir")));

        langs = spy(new TaskExecutorImpl());
        TmcLangsHolder.set(langs);
        Exercise ex = new Exercise("name", "course");
        command = new SubmitAdaptiveExerciseToSkillifier(mockObserver, ex, factory);

        arithFuncsTempDir = TestUtils.getProject(this.getClass(), "arith_funcs");
        when(mockExercise.getExerciseDirectory(any(Path.class))).thenReturn(arithFuncsTempDir);
        when(settings.getLocale()).thenReturn(new Locale("FI"));
    }

    @Test(timeout = 10000)
    public void testCall() throws Exception {
        verifyZeroInteractions(mockObserver);
        doReturn(new byte[0]).when(langs).compressProject(any(Path.class));
        when(
            factory.getSubmittingExerciseToSkillifierTask(
                any(Exercise.class), any(byte[].class), any(Map.class)))
            .thenReturn(
                new Callable<TmcServerCommunicationTaskFactory.SubmissionResponse>() {
                    @Override
                    public TmcServerCommunicationTaskFactory.SubmissionResponse call() throws Exception {
                        return STUB_RESPONSE;
                    }
                });
        when(factory.getSubmissionFetchTask(any(URI.class)))
            .thenReturn(
                new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        return STUB_PROSESSING_RESPONSE;
                    }
                })
            .thenReturn(
                new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        return STUB_PROSESSING_DONE_RESPONSE;
                    }
                });

        SubmissionResult result = command.call();
        assertEquals(result.getStatus(), SubmissionResult.Status.OK);
    }

}
