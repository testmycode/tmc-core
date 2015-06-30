package feature.logout;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import hy.tmc.cli.configuration.ClientData;
import hy.tmc.cli.configuration.ConfigHandler;
import hy.tmc.cli.frontend.communication.server.Server;
import hy.tmc.cli.testhelpers.TestClient;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import java.io.IOException;

public class LogoutSteps {

    private int port;
    private Thread serverThread;
    private TestClient testClient;
    private Server server;

    /**
     * Read port from ConfigHandler, create test client and server.
     * @throws IOException if server creating fails
     */
    @Before
    public void initializeServer() throws IOException {
        ClientData.clearUserData();
        server = new Server();
        port = new ConfigHandler().readPort();
        serverThread = new Thread(server);
        serverThread.start();
        testClient = new TestClient(port);
    }

    @Given("^logout command\\.$")
    public void logout_command() throws Throwable {
        ClientData.setUserData("Samu", "Samuonparas3443434334");
        testClient.sendMessage("logout");
    }

    @When("^user sees message\\.$")
    public void user_sees_message() throws Throwable {
        String output = testClient.reply();
        assertTrue(output.contains("cleared"));
    }

    @Then("^user data should be cleared\\.$")
    public void user_data_should_be_cleared() throws Throwable {
        assertFalse(ClientData.userDataExists());
    }

    @Given("^logout command without being logged in\\.$")
    public void logout_command_without_being_logged_in() throws Throwable {
        testClient.sendMessage("logout");
    }

    @When("^nothing should happen\\.$")
    public void nothing_should_happen() throws Throwable {
        assertFalse(ClientData.userDataExists());
    }

    @Then("^user sees error message\\.$")
    public void user_sees_error_message() throws Throwable {
        assertTrue(testClient.reply().contains("Nobody"));
    }

    /**
     * Clear ClientData and close server.
     * @throws IOException if server closing fails
     */
    @After
    public void closeAll() throws IOException {
        ClientData.clearUserData();
        server.close();
        serverThread.interrupt();
    }
}
