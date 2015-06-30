package feature.runtests;

import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import hy.tmc.cli.backend.Mailbox;
import hy.tmc.cli.configuration.ClientData;
import hy.tmc.cli.frontend.communication.commands.RunTests;
import hy.tmc.cli.frontend.communication.server.ProtocolException;
import hy.tmc.cli.synchronization.TmcServiceScheduler;
import hy.tmc.cli.testhelpers.ProjectRootFinderStub;
import org.junit.After;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.langs.NoLanguagePluginFoundException;
import java.io.UnsupportedEncodingException;

public class TmcTestsSteps {

    private RunTests testRunner;
    private String output;

    @Before
    public void setUp() {
        ClientData.setProjectRootFinder(new ProjectRootFinderStub());
        ClientData.clearUserData();
        Mailbox.create();
        ClientData.setUserData("test", "1234");
        TmcServiceScheduler.disablePolling();
    }

    @Given("^the user is in the exercise directory \"(.*?)\"$")
    public void theUserIsInTheExerciseDirectory(String exerciseDirectory) {
        testRunner = new RunTests();
        testRunner.setParameter("path", exerciseDirectory);
    }

    @When("^the user runs the tests$")
    public void theUserRunsTheTests() throws ProtocolException, NoLanguagePluginFoundException {
        output = testRunner.parseData(testRunner.call()).get();
    }

    /**
     * Test case that all tmc tests pass.
     */
    @Then("^the user sees that all tests have passed\\.$")
    public void theUserSeesAllTestsPassing() {
        assertTrue(output.contains("\u001B[32mAll tests passed.\u001B[0m You can now submit"));
    }

    /**
     * Test case when some tests fail and user gets information.
     */
    @Then("^the user sees which tests have failed$")
    public void theUserSeesWhichTestsHaveFailed() {
        assertTrue(output.contains("Some tests failed:"));
        assertTrue(output.contains("\u001B[31m1 tests failed:\n"));
        assertTrue(output.contains("FAILED \u001B[0mNimiTest test: Et tulostanut mitään!"));
    }

    /**
     * User should get both information about passed tests and failed tests.
     */
    @Then("^the user sees both passed and failed tests$")
    public void theUserSeesBothPassedAndFailedTests() {
        assertTrue(output.contains("1 tests passed"));
        assertTrue(output.contains("2 tests failed"));
    }


    @Given("^polling for reviews is not in progress and exercise path is \"(.*?)\"$")
    public void polling_for_reviews_is_not_in_progress_and_exercise_path_is(String path) throws Throwable {
        TmcServiceScheduler.enablePolling();
        testRunner = new RunTests();
        testRunner.setParameter("path", path);
        assertFalse(TmcServiceScheduler.isRunning());
    }

    @Given("^the user gives the vim flag$")
    public void gives_the_vim_flag() throws Throwable {
        testRunner.setParameter("--vim", "");
    }

    @Then("^the user sees that all tests have passed formatted with vim formatter\\.$")
    public void the_user_sees_that_all_tests_have_passed_formatted_with_vim_formatter() throws Throwable {
        assertTrue(output.contains("All tests passed. You can now submit"));
    }

    @After
    public void clean() throws InterruptedException {
        Mailbox.destroy();
        TmcServiceScheduler.getScheduler().stop();
        ClientData.clearUserData();
    }
}
