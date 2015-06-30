package feature.login;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertTrue;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import hy.tmc.cli.configuration.ConfigHandler;
import hy.tmc.cli.frontend.communication.server.Server;
import hy.tmc.cli.testhelpers.TestClient;

import org.junit.Rule;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import hy.tmc.cli.configuration.ClientData;


import java.io.IOException;


public class LoginSteps {

    private int port;

    private Thread serverThread;
    private TestClient testClient;
    private Server server;

    private ConfigHandler configHandler; // writes the test address
    private WireMockServer wireMockServer;

    private static final String SERVER_URI = "127.0.0.1";
    private static final int SERVER_PORT = 3333;
    private static final String SERVER_ADDRESS = "http://" + SERVER_URI + ":" + SERVER_PORT;

    @Rule
    WireMockRule wireMockRule = new WireMockRule();

    /**
     * Initialize server, set address and start thread.
     * @throws IOException if server creating fails
     */
    @Before
    public void initializeServer() throws IOException {
        configHandler = new ConfigHandler();
        configHandler.writeServerAddress(SERVER_ADDRESS);
        ClientData.clearUserData();
        server = new Server();
        port = new ConfigHandler().readPort();
        serverThread = new Thread(server);
        serverThread.start();
        testClient = new TestClient(port);


        startWireMock();
    }

    private void startWireMock() {
        wireMockServer = new WireMockServer(wireMockConfig().port(SERVER_PORT));
        WireMock.configureFor(SERVER_URI, SERVER_PORT);
        wireMockServer.start();

        wireMockServer.stubFor(get(urlEqualTo("/user"))
                .withHeader("Authorization", containing("Basic dGVzdDoxMjM0"))
                .willReturn(
                        aResponse()
                        .withStatus(200)
                )
        );
    }

    @When("^user gives username \"(.*?)\" and password \"(.*?)\"$")
    public void user_gives_uname_and_password(String username, String password) throws Throwable {
        testClient.sendMessage("login username " + username + " password " + password);
    }

    @Then("^user should see result\\.$")
    public void user_should_see_result() {
        assertTrue(testClient.reply().contains("Saved userdata in session"));
    }

    /**
     * Close server and wiremock after test.
     * @throws IOException if server closing fails
     */
    @After
    public void closeAll() throws IOException {
        server.close();
        serverThread.interrupt();
        WireMock.reset();
        wireMockServer.stop();
        configHandler.writeServerAddress("http://tmc.mooc.fi/staging");
    }
}
