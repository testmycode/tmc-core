package feature.frontend;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import static org.junit.Assert.assertTrue;

import hy.tmc.cli.configuration.ConfigHandler;
import hy.tmc.cli.frontend.communication.server.Server;
import hy.tmc.cli.testhelpers.TestClient;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;

import java.io.IOException;

public class FrontendSteps {

    private int port;

    private Thread serverThread;
    private Server server;
    private TestClient testClient;

    /**
     * Set up server and testclient.
     * @throws IOException if server initializing fails
     */
    @Before
    public void setUpServer() throws IOException {
        server = new Server();
        port = new ConfigHandler().readPort();
        serverThread = new Thread(server);
        serverThread.start();
        testClient = new TestClient(port);
    }

    @Given("^help command\\.$")
    public void help_command() throws Throwable {
        testClient.sendMessage("help");
    }

    /**
     * Tests that output contains available commands.
     * @throws Throwable if something fails
     */
    @Then("^output should contains commands\\.$")
    public void output_should_contains_commands() throws Throwable {
        String contents = testClient.reply();
        assertTrue(contents.contains("Available commands:"));
    }

    @After
    public void closeServer() throws IOException {
        server.close();
        serverThread.interrupt();
    }
}
