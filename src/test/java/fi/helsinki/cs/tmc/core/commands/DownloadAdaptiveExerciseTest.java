package fi.helsinki.cs.tmc.core.commands;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import fi.helsinki.cs.tmc.core.communication.TmcServerCommunicationTaskFactory;
import fi.helsinki.cs.tmc.core.communication.oauth2.Oauth;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.domain.Theme;
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

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
    @Mock
    Oauth oauth;
    @Mock
    Theme mockTheme;

    private Command<Exercise> command;
    TaskExecutor langs;
    Path arithFuncsTempDir;
    Theme theme;

    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        langs = spy(new TaskExecutorImpl());
        TmcSettingsHolder.set(settings);
        TmcLangsHolder.set(langs);
        arithFuncsTempDir = testFolder.getRoot().toPath().resolve("arith_funcs");
        command = new DownloadAdaptiveExerciseByTheme(mockObserver, factory, mockTheme);

        doCallRealMethod().when(langs).extractProject(any(Path.class), any(Path.class));
        mockExerciseOne.setName("ex1");
        mockExerciseOne.setCourseName("course1");


    }

    @Test
    public void testDownloadAndExtractSuccess() throws Exception {
        setUpMocks();

        Exercise exercise = command.call();

        verify(factory).getAdaptiveExerciseByTheme(mockTheme);
        verify(factory).getDownloadingAdaptiveExerciseZipTask(mockExerciseOne);

        verifyNoMoreInteractions(factory);

        assertTrue(Files.exists(arithFuncsTempDir));
    }

    @Test
    public void testDownloadAndExtractFailure() throws Exception {
        setUpMocks();
        when(mockGetAdaptiveExercise.call()).thenReturn(null);

        Exercise exercise = command.call();

        verify(factory).getAdaptiveExerciseByTheme(mockTheme);

        verifyNoMoreInteractions(factory);

    }

    private void setUpMocks() throws Exception {
        verifyZeroInteractions(langs);

        when(mockTheme.getName()).thenReturn("testTheme");
        when(factory.getAdaptiveExerciseByTheme(any(Theme.class))).thenReturn(mockGetAdaptiveExercise);
        when(mockGetAdaptiveExercise.call()).thenReturn(mockExerciseOne);

        when(mockExerciseOne.getExtractionTarget(any(Path.class))).thenReturn(arithFuncsTempDir);
        when(settings.getTmcProjectDirectory()).thenReturn(testFolder.getRoot().toPath());
        when(oauth.getToken()).thenReturn("testToken");

        when(factory.getDownloadingAdaptiveExerciseZipTask(mockExerciseOne))
                .thenReturn(
                        new Callable<byte[]>() {
                            @Override
                            public byte[] call() throws Exception {
                                return Files.readAllBytes(
                                        TestUtils.getZip(this.getClass(), "arith_funcs.zip"));
                            }
                        });
    }

}
