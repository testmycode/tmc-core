package fi.helsinki.cs.tmc.core.commands;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import fi.helsinki.cs.tmc.core.utils.TestUtils;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Callable;

import static com.google.common.truth.Truth.assertThat;

import fi.helsinki.cs.tmc.core.communication.TmcServerCommunicationTaskFactory;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import com.google.common.collect.Lists;
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

import java.net.URI;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author fogh
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

    private Command<Exercise> command;
    TaskExecutor langs;
    Path arithFuncsTempDir;

    @Before
    public void setUp() throws IOException {
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
    public void checkExerciseZipUrl() throws Exception {
        setUpMocks();
        DownloadAdaptiveExercise e = new DownloadAdaptiveExercise(mockObserver);
        Exercise exercise = command.call();
    }

    @Test
    public void testDownloadAndExtractSuccess() throws Exception {
        setUpMocks();

        Exercise exercise = command.call();

        verify(factory).getAdaptiveExercise();
        verify(factory).getDownloadingExerciseZipTask(mockExerciseOne);

        verifyNoMoreInteractions(factory);

        assertTrue(Files.exists(arithFuncsTempDir));
        // TODO: check for contents?
    }

    @Test
    public void testDownloadAndExtractFailure() throws Exception {
        setUpMocks();
        when(mockGetAdaptiveExercise.call()).thenReturn(null);

        Exercise exercise = command.call();

        verify(factory).getAdaptiveExercise();

        verifyNoMoreInteractions(factory);

        // TODO: check for contents?
    }

    private void setUpMocks() throws Exception {
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
    }

    @Test
    public void testDownloadAndExtractSuccessWithRealZip() throws Exception {
        verifyZeroInteractions(langs);
        TmcServerCommunicationTaskFactory realFactory = new TmcServerCommunicationTaskFactory();
        assertNotNull(TmcSettingsHolder.get());
        command = new DownloadAdaptiveExercise(mockObserver, realFactory);

        when(settings.getTmcProjectDirectory()).thenReturn(Paths.get(System.getProperty("user.dir")));

        Exercise exercise = command.call();

        verifyNoMoreInteractions(factory);

        assertTrue(Files.exists(Paths.get(System.getProperty("user.dir"))));
        Files.deleteIfExists(Paths.get(System.getProperty("user.dir")).resolve("porsk!"));
        // TODO: check for contents?
    }
}
