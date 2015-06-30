
package feature.submit;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import cucumber.api.PendingException;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import hy.tmc.cli.backend.Mailbox;
import hy.tmc.cli.configuration.ClientData;
import hy.tmc.cli.configuration.ConfigHandler;
import hy.tmc.cli.frontend.communication.server.Server;
import hy.tmc.cli.synchronization.TmcServiceScheduler;
import hy.tmc.cli.testhelpers.MailExample;
import hy.tmc.cli.testhelpers.ProjectRootFinderStub;
import hy.tmc.cli.testhelpers.TestClient;
import hy.tmc.cli.testhelpers.Wiremocker;
import java.io.File;
import java.io.IOException;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


import org.junit.Rule;

public class SubmitSteps {

    private int port;

    private Thread serverThread;
    private TestClient testClient;
    private Server server;

    private ConfigHandler configHandler;
    private WireMockServer wireMockServer;

    @Rule
    WireMockRule wireMockRule = new WireMockRule();
    private String submitCommand;

    /**
     * Writes wiremock-serveraddress to config-file, starts wiremock-server and defines routes for two scenario.
     */
    @Before
    public void initializeServer() throws IOException {
        configHandler = new ConfigHandler();
        configHandler.writeServerAddress("http://127.0.0.1:8080");
        ClientData.clearUserData();
        server = new Server();
        port = configHandler.readPort();
        serverThread = new Thread(server);
        serverThread.start();
        testClient = new TestClient(port);

        TmcServiceScheduler.disablePolling();
        Mailbox.create();
        ClientData.setProjectRootFinder(new ProjectRootFinderStub());

        Wiremocker mocker = new Wiremocker();
        wireMockServer = mocker.wiremockSubmitPaths();
        mocker.wireMockSuccesfulSubmit(wireMockServer);
        mocker.wireMockExpiredSubmit(wireMockServer);
        mocker.wiremockFailingSubmit(wireMockServer);
    }


    @Given("^user has logged in with username \"(.*?)\" and password \"(.*?)\"$")
    public void user_has_logged_in_with_username_and_password(String username, String password) throws Throwable {
        testClient.sendMessage("login username " + username + " password " + password);
        checkForMessages();
    }

    private void checkForMessages() throws IOException, InterruptedException {
        Thread.sleep(300);
    }

    @When("^user gives command submit with valid path \"(.*?)\" and exercise \"(.*?)\"$")
    public void user_gives_command_submit_with_valid_path_and_exercise(String pathFromProjectRoot, String exercise) throws Throwable {
        submitCommand = "submit path " + System.getProperty("user.dir") + pathFromProjectRoot + File.separator + exercise;
    }
    
    @When("^flag \"(.*?)\"$")
    public void flag(String flag) throws Throwable {
        this.submitCommand += " " + flag;
    }

    @When("^user executes the command$")
    public void user_executes_the_command() throws Throwable {
        testClient.init();
        checkForMessages();
        testClient.sendMessage(submitCommand);
        checkForMessages();
    }

    @Then("^user will see all test passing$")
    public void user_will_see_all_test_passing() throws Throwable {
        final String result = testClient.getAllFromSocket();
        assertTrue(result.contains("All tests passed"));
    }

    @Then("^user will see the some test passing$")
    public void user_will_see_the_some_test_passing() throws Throwable {
        final String result = testClient.getAllFromSocket().toLowerCase();
        assertTrue(result.contains("some tests failed"));
    }

    @Then("^user will see a message which tells that exercise is expired\\.$")
    public void user_will_see_a_message_which_tells_that_exercise_is_expired() throws Throwable {
        final String result = testClient.getAllFromSocket();
        assertTrue(result.contains("expired"));
    }

    @Given("^the user has mail in the mailbox$")
    public void the_user_has_mail_in_the_mailbox() throws Throwable {
        Mailbox.getMailbox().get().fill(MailExample.reviewExample());
    }

    @Then("^user will see the new mail$")
    public void user_will_see_the_new_mail() throws Throwable {
        String result = testClient.getAllFromSocket();
        System.out.println("Result: " + result);
        assertTrue(result.contains("unread code reviews"));
    }

    @Given("^polling for reviews is not in progress$")
    public void polling_for_reviews_is_not_in_progress() throws Throwable {
        TmcServiceScheduler.enablePolling();
        assertFalse(TmcServiceScheduler.isRunning());
    }

    @Then("^the polling will be started$")
    public void the_polling_will_be_started() throws Throwable {
        assertTrue(TmcServiceScheduler.isRunning());
        TmcServiceScheduler.getScheduler().stop();
    }

    @When("^user gives command submit with path \"([^\"]*)\" and exercise \"([^\"]*)\"$")
    public void user_gives_command_submit_with_path_and_exercise(String pathFromProjectRoot, String exercise) throws Throwable {
        submitCommand = "submit path " + System.getProperty("user.dir") + pathFromProjectRoot + File.separator + exercise;
    }

    /**
     * Returns everything to it's original state.
     */
    @After
    public void closeAll() throws IOException {
        server.close();
        serverThread.interrupt();
        wireMockServer.stop();
        configHandler.writeServerAddress("http://tmc.mooc.fi/staging");
        ClientData.clearUserData();
        Mailbox.destroy();
        ClientData.setProjectRootFinder(null);
    }
}
