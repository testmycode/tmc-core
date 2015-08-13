package fi.helsinki.cs.tmc.core.commands;

import static org.junit.Assert.assertEquals;

import fi.helsinki.cs.tmc.core.CoreTestSettings;
import fi.helsinki.cs.tmc.langs.domain.RunResult;

import org.junit.Before;
import org.junit.Test;

public class RunTestsTest {

    private CoreTestSettings settings;

    @Before
    public void setup() {
        settings = new CoreTestSettings();
    }

    @Test
    public void testfailingRunTests() throws Exception {
        String path =
                System.getProperty("user.dir")
                        + "/testResources/2014-mooc-no-deadline/viikko1/Viikko1_001.Nimi/src";

        RunResult result = new RunTests(path, settings).call();

        assertEquals(RunResult.Status.TESTS_FAILED, result.status);
    }

    @Test
    public void successRunTests() throws Exception {
        String path =
                System.getProperty("user.dir")
                        + "/testResources/successExercise/viikko1/Viikko1_001.Nimi/src";
        System.out.println(path);

        RunResult result = new RunTests(path, settings).call();

        assertEquals(RunResult.Status.PASSED, result.status);
    }
}
