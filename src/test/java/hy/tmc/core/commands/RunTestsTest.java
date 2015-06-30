package hy.tmc.core.commands;

import hy.tmc.core.configuration.ClientData;

import hy.tmc.core.exceptions.ProtocolException;

import org.junit.Before;
import org.junit.Test;

import org.junit.After;

public class RunTestsTest {

    private RunTests runTests;

    /**
     * Create FrontendStub and RunTests command.
     */
    @Before
    public void setup() {
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
        ClientData.clearUserData();
    }
}