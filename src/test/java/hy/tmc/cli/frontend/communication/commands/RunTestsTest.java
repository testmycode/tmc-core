package hy.tmc.cli.frontend.communication.commands;

import fi.helsinki.cs.tmc.langs.NoLanguagePluginFoundException;
import hy.tmc.cli.configuration.ClientData;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import hy.tmc.core.exceptions.ProtocolException;
import hy.tmc.cli.synchronization.TmcServiceScheduler;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import org.junit.After;

public class RunTestsTest {

    private RunTests runTests;

    /**
     * Create FrontendStub and RunTests command.
     */
    @Before
    public void setup() {
        TmcServiceScheduler.disablePolling();
        ClientData.setUserData("test", "1234");
        runTests = new RunTests();
    }

    /**
     * Check that data checking success.
     */
    @Test
    public void testCheckDataSuccess() throws ProtocolException {
        RunTests rt = new RunTests();
        rt.setParameter("path", "/home/tmccli/uolevipuistossa");
        rt.checkData();
    }

    /**
     * Check that if user didn't give correct data, data checking fails.
     */
    @Test(expected = ProtocolException.class)
    public void testCheckDataFail() throws ProtocolException {
        RunTests rt = new RunTests();
        rt.checkData();
    }
    
    @After
    public void clear() {
        TmcServiceScheduler.enablePolling();
        ClientData.clearUserData();
    }
}