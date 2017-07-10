package fi.helsinki.cs.tmc.core.commands;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

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

import com.google.common.base.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.net.URI;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;

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

    private static final String STUB_PROSESSING_ERRORED_RESPONSE = "{status : \"ERROR\", error: \"failed to submit the exercise\"}";
    private static final String STUB_PROSESSING_DONE_RESPONSE = "{status: \"OK\"}";
    private static final String STUB_PROSESSING_RESPONSE = "{status: \"PROCESSING\"}";

    private Command<SubmissionResult> command;
    private Path arithFuncsTempDir;
    private TaskExecutor langs;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        TmcSettingsHolder.set(settings);

        langs = spy(new TaskExecutorImpl());
        TmcLangsHolder.set(langs);
        TmcSettingsHolder.set(settings);
        Exercise ex = new Exercise("Osa02_01.WilliamLovelace", "Example");
        command = new SubmitAdaptiveExerciseToSkillifier(mockObserver, mockExercise, factory);

        arithFuncsTempDir = TestUtils.getProject(this.getClass(), "arith_funcs");
        when(mockExercise.getExerciseDirectory(any(Path.class))).thenReturn(arithFuncsTempDir);
        when(settings.getLocale()).thenReturn(new Locale("FI"));
        Optional<String> mockToken = Optional.of("testToken");
        settings.setToken(mockToken);

        when(factory.getSkillifierUrl(anyString())).thenReturn(URI.create("www.example.com"));
    }

    @Test(timeout = 10000)
    public void testSuccessfulSubmit() throws Exception {

        verifyZeroInteractions(mockObserver);
        doReturn(new byte[0]).when(langs).compressProject(any(Path.class));
        when(
                factory.getSubmittingExerciseToSkillifierTask(
                any(Exercise.class), any(byte[].class), any(Map.class)))
                .thenReturn(
                new Callable<TmcServerCommunicationTaskFactory.SubmissionResponse>() {
                        @Override
                        public TmcServerCommunicationTaskFactory.SubmissionResponse call() throws Exception {
                            return null;
                        }
                    });
        when(factory.getSubmissionFromSkillifierFetchTask())
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

        assertEquals(SubmissionResult.Status.OK, result.getStatus());
    }

    @Test(timeout = 10000)
    public void testUnsuccessfulSubmit() throws Exception {

        verifyZeroInteractions(mockObserver);
        doReturn(new byte[0]).when(langs).compressProject(any(Path.class));
        when(
                factory.getSubmittingExerciseToSkillifierTask(
                any(Exercise.class), any(byte[].class), any(Map.class)))
                .thenReturn(
                new Callable<TmcServerCommunicationTaskFactory.SubmissionResponse>() {
                        @Override
                        public TmcServerCommunicationTaskFactory.SubmissionResponse call() throws Exception {
                            return null;
                        }
                    });
        when(
                factory.getSubmissionFromSkillifierFetchTask())
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
                            return STUB_PROSESSING_ERRORED_RESPONSE;
                        }
                    });

        SubmissionResult result = command.call();

        assertEquals(SubmissionResult.Status.ERROR, result.getStatus());
        assertEquals("failed to submit the exercise", result.getError());
    }
}
