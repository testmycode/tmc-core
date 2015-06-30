package feature.listcourses;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import hy.tmc.cli.configuration.ConfigHandler;
import hy.tmc.cli.frontend.communication.server.Server;
import hy.tmc.cli.testhelpers.ExampleJson;
import hy.tmc.cli.testhelpers.TestClient;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import hy.tmc.cli.configuration.ClientData;

import java.io.IOException;

public class ListCoursesSteps {
    
    private int port;
    
    private Thread serverThread;
    private Server server;
    private TestClient testClient;
    private boolean testThrown;
    
    private ConfigHandler configHandler; // writes the test address
    private WireMockServer wireMockServer;

    private static final String SERVER_URI = "127.0.0.1";
    private static final int SERVER_PORT = 7777;
    private static final String SERVER_ADDRESS = "http://" + SERVER_URI + ":" + SERVER_PORT;

    /**
     * Setups client's config and starts WireMock.
     */
    @Before
    public void setUpServer() throws IOException {
        configHandler = new ConfigHandler();
        configHandler.writeServerAddress(SERVER_ADDRESS);
        
        server = new Server();
        port = configHandler.readPort();
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
        wireMockServer.stubFor(get(urlEqualTo(new ConfigHandler().coursesExtension))
                .withHeader("Authorization", containing("Basic dGVzdDoxMjM0"))
                .willReturn(
                        aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(ExampleJson.allCoursesExample)
                )
        );
        
    }
    
    @Given("^user has logged in with username \"(.*?)\" and password \"(.*?)\"\\.$")
    public void user_has_logged_in_with_username_and_password(String username,
                                                              String password) throws Throwable {
        testClient.sendMessage("login username " + username + " password " + password);
        testClient.getAllFromSocket();
        testClient.init();
    }
    
    @When("^user gives command listCourses\\.$")
    public void user_gives_command_listCourses() throws Throwable {
        testClient.sendMessage("listCourses");
    }

    @Then("^output should contain more than one line$")
    public void output_should_contain_more_than_one_line() throws Throwable {
        String content = testClient.reply();
        assertTrue(content.contains("id"));
        serverThread.interrupt();
    }
    
    @Given("^user has not logged in$")
    public void user_has_not_logged_in() throws Throwable {
        testClient = new TestClient(port);
    }

    /**
     * User sends command "listCourses" to server.
     */
    @When("^user writes listCourses\\.$")
    public void user_writes_listCourses() throws Throwable {
        testThrown = false;
        try {
            testClient.sendMessage("listCourses");
        } catch (Exception e) {
            testThrown = true;
        }
    }
    
    @Then("^exception should be thrown$")
    public void exception_should_be_thrown() throws Throwable {
        assertFalse(testThrown);
        serverThread.interrupt();
    }

    /**
     * Shuts down the server and WireMock after scenario.
     */
    @After
    public void closeServer() throws IOException {
        server.close();
        serverThread.interrupt();
        WireMock.reset();
        wireMockServer.stop();
        configHandler.writeServerAddress("http://tmc.mooc.fi/staging");
    }
    
}
