package fi.helsinki.cs.tmc.core.commands;

import static org.junit.Assert.assertEquals;

import fi.helsinki.cs.tmc.core.CoreTestSettings;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;
import fi.helsinki.cs.tmc.langs.domain.RunResult;

import com.google.common.util.concurrent.ListenableFuture;

import org.junit.Before;
import org.junit.Test;

public class RunTestsTest {

    private RunTests runTests;
    private CoreTestSettings settings;

    /**
     * Create FrontendStub and RunTests command.
     */
    @Before
    public void setup() {
        settings = new CoreTestSettings();
        settings.setUsername("test");
        settings.setUsername("1234");
        runTests = new RunTests(settings);
    }

    /**
     * Check that data checking success.
     */
    @Test
    public void testCheckDataSuccess() throws TmcCoreException {
        settings = new CoreTestSettings();
        RunTests rt = new RunTests(settings);
        rt.setParameter("path", "/home/tmccli/uolevipuistossa");
        rt.checkData();
    }

    /**
     * Check that if user didn't give correct data, data checking fails.
     */
    @Test(expected = TmcCoreException.class)
    public void testCheckDataFail() throws TmcCoreException {
        settings = new CoreTestSettings();
        RunTests rt = new RunTests(settings);
        rt.checkData();
    }

    @Test
    public void failingRunTests() throws Exception {
        String path =
                System.getProperty("user.dir")
                        + "/testResources/2014-mooc-no-deadline/viikko1/Viikko1_001.Nimi/src";
        TmcCore core = new TmcCore();
        ListenableFuture<RunResult> testFuture = core.test(path, settings);
        RunResult result = testFuture.get();

        assertEquals("TESTS_FAILED", result.status.toString());
    }

    @Test
    public void successRunTests() throws Exception {
        String path =
                System.getProperty("user.dir")
                        + "/testResources/successExercise/viikko1/Viikko1_001.Nimi/src";
        TmcCore core = new TmcCore();
        ListenableFuture<RunResult> testFuture = core.test(path, settings);
        RunResult result = testFuture.get();

        assertEquals("PASSED", result.status.toString());
    }
}
