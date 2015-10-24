package fi.helsinki.cs.tmc.core.commands;

import static org.junit.Assert.assertEquals;

import fi.helsinki.cs.tmc.core.CoreTestSettings;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;
import fi.helsinki.cs.tmc.langs.domain.RunResult;

import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

public class RunTestsTest {

    private CoreTestSettings settings;

    @Before
    public void setup() {
        settings = new CoreTestSettings();
    }

    @Test(expected = TmcCoreException.class)
    public void testThrowsExceptionOnInvalidPath() throws TmcCoreException {
        new RunTests(settings,Paths.get("nosuch")).call();
    }

    @Test
    public void testfailingRunTests() throws TmcCoreException {
        Path path =
                Paths.get(System.getProperty("user.dir")
                        + "/src/test/resources/local-test-course/failingExercise");

        RunResult result = new RunTests(settings, path).call();

        assertEquals(RunResult.Status.TESTS_FAILED, result.status);
    }

    @Test
    public void successRunTests() throws TmcCoreException {
        Path path =
                Paths.get(System.getProperty("user.dir")
                        + "/src/test/resources/local-test-course/successExercise");

        RunResult result = new RunTests(settings, path).call();

        assertEquals(RunResult.Status.PASSED, result.status);
    }
}
