package fi.helsinki.cs.tmc.core.commands;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import fi.helsinki.cs.tmc.core.commands.DownloadAdaptiveExercise;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.holders.TmcLangsHolder;
import fi.helsinki.cs.tmc.core.holders.TmcSettingsHolder;
import fi.helsinki.cs.tmc.core.utils.MockSettings;
import fi.helsinki.cs.tmc.langs.util.TaskExecutor;
import fi.helsinki.cs.tmc.langs.util.TaskExecutorImpl;

import java.util.concurrent.Callable;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mockito.Mock;
import static org.mockito.Mockito.spy;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

/**
 *
 * @author sakuolin
 */
public class DownloadAdaptiveExerciseTest {
    
    @Mock ProgressObserver mockObserver;
    @Spy TmcSettings settings = new MockSettings();
    @Mock Exercise exercise;

    TaskExecutor langs;

    private Command<Exercise> command;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        langs = spy(new TaskExecutorImpl());
        TmcSettingsHolder.set(settings);
        TmcLangsHolder.set(langs);
        command = new DownloadAdaptiveExercise(mockObserver);
    }
    
    @Test
    public void exerciseExists() throws Exception {
        exercise = command.call();
        assertNotEquals(null, exercise);
    }
    
}
