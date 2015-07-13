package hy.tmc.core.commands;

import hy.tmc.core.ClientTmcSettings;

import hy.tmc.core.exceptions.TmcCoreException;

import org.junit.Before;
import org.junit.Test;


public class RunTestsTest {

    private RunTests runTests;
    ClientTmcSettings settings;

    /**
     * Create FrontendStub and RunTests command.
     */
    @Before
    public void setup() {
        settings = new ClientTmcSettings();
        settings.setUsername("test");
        settings.setUsername("1234");
        runTests = new RunTests(settings);
    }

    /**
     * Check that data checking success.
     */
    @Test
    public void testCheckDataSuccess() throws TmcCoreException {
        settings = new ClientTmcSettings();
        RunTests rt = new RunTests(settings);
        rt.setParameter("path", "/home/tmccli/uolevipuistossa");
        rt.checkData();
    }

    /**
     * Check that if user didn't give correct data, data checking fails.
     */
    @Test(expected = TmcCoreException.class)
    public void testCheckDataFail() throws TmcCoreException {
        settings = new ClientTmcSettings();
        RunTests rt = new RunTests(settings);
        rt.checkData();
    }
    
}