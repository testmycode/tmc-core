package hy.tmc.cli.synchronization;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import hy.tmc.cli.domain.Course;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;

public class TmcServiceSchedulerTest {

    private TmcServiceScheduler tmcServiceScheduler;

    @Before
    public void before() throws Exception {
        tmcServiceScheduler = TmcServiceScheduler.getScheduler();
    }

    @After
    public void after() throws Exception {
    }

    @Test
    public void onlyOneInstanceCanBeInitialised() {
        tmcServiceScheduler = TmcServiceScheduler.getScheduler();
        tmcServiceScheduler = TmcServiceScheduler.getScheduler();
        tmcServiceScheduler = TmcServiceScheduler.getScheduler();
        assertTrue(this.tmcServiceScheduler == TmcServiceScheduler.getScheduler());
    }

    @Test
    public void whenDisabledNoNewTasksAreAddedWhenStarting() throws Exception {
        TmcServiceScheduler.disablePolling();
        TmcServiceScheduler.startIfNotRunning(new Course());
        assertTrue(TmcServiceScheduler.getInitialisedTasks().isEmpty());
    }

    @Test
    public void disableMimicsTheRunningState() {
        TmcServiceScheduler.disablePolling();
        assertTrue(TmcServiceScheduler.isRunning());
    }

    @Test
    public void defaultIsAlwaysDisabled() {
        assertFalse(TmcServiceScheduler.isRunning());
    }

    @Test
    public void enableNegatesDisable() {
        TmcServiceScheduler.disablePolling();
        TmcServiceScheduler.enablePolling();
        assertFalse(TmcServiceScheduler.isRunning());
    }

    @Test
    public void stopCreatesANewInstance() {
        TmcServiceScheduler.getScheduler().stop();
        assertTrue(tmcServiceScheduler != TmcServiceScheduler.getScheduler());
    }




} 
