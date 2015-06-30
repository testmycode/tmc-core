package feature.paste;


import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertTrue;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
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
import hy.tmc.cli.testhelpers.ExampleJson;
import hy.tmc.cli.testhelpers.MailExample;
import hy.tmc.cli.testhelpers.ProjectRootFinderStub;
import hy.tmc.cli.testhelpers.TestClient;
import java.io.File;
import java.io.IOException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.junit.Rule;


public class PasteSteps {

    private int port;

    private Thread serverThread;
    private TestClient testClient;
    private Server server;

    private ConfigHandler configHandler; // writes the test address
    private WireMockServer wireMockServer;
    private String pasteCommand;

    @Rule
    WireMockRule wireMockRule = new WireMockRule();

    @Before
    public void initializeServer() throws IOException {
        TmcServiceScheduler.disablePolling();

        configHandler = new ConfigHandler();
        configHandler.writeServerAddress("http://127.0.0.1:8080");

        server = new Server();
        port = configHandler.readPort();
        serverThread = new Thread(server);
        serverThread.start();
        testClient = new TestClient(port);
        ClientData.setUserData("Chuck", "Norris");
        ClientData.setProjectRootFinder(new ProjectRootFinderStub());
        startWireMock();
    }

    @After
    public void closeAll() throws IOException, InterruptedException {
        Mailbox.destroy();
        server.close();
        serverThread.interrupt();
        wireMockServer.stop();
        configHandler.writeServerAddress("http://tmc.mooc.fi/staging");
        ClientData.clearUserData();
    }

    private void startWireMock() {
        wireMockServer = new WireMockServer();
        wireMockServer.start();

        wireMockServer.stubFor(get(urlEqualTo("/user"))
                .withHeader("Authorization", containing("Basic dGVzdDoxMjM0"))
                .willReturn(aResponse().withStatus(200)));
        wiremockGET("/courses.json?api_version=7", ExampleJson.allCoursesExample);
        wiremockGET("/courses/3.json?api_version=7", ExampleJson.courseExample);
        wiremockPOST("/exercises/286/submissions.json?api_version=7&paste=1", ExampleJson.pasteResponse);
        wiremockGET("/submissions/1781.json?api_version=7", ExampleJson.successfulSubmission);

    }

    private void wiremockGET(final String urlToMock, final String returnBody) {
        wireMockServer.stubFor(get(urlEqualTo(urlToMock))
                .willReturn(aResponse()
                        .withBody(returnBody)
                )
        );
    }

    private void wiremockPOST(final String urlToMock, final String returnBody) {
        wireMockServer.stubFor(post(urlEqualTo(urlToMock))
                .willReturn(aResponse()
                        .withBody(returnBody)
                )
        );
    }

    @Given("^user has logged in with username \"(.*?)\" and password \"(.*?)\"$")
    public void user_has_logged_in_with_username_and_password(String username, String password) throws Throwable {
        testClient.sendMessage("login username " + username + " password " + password);
    }

    @When("^user gives command paste with valid path \"(.*?)\" and exercise \"(.*?)\"$")
    public void user_gives_command_paste_with_valid_path_and_exercise(String path, String exercise) throws Throwable {
        this.pasteCommand = "paste path ";
        String pastePath = System.getProperty("user.dir") + path + File.separator + exercise;
        this.pasteCommand = pasteCommand + pastePath;
    }

    @When("^flag \"(.*?)\"$")
    public void flag(String flag) throws Throwable {
        this.pasteCommand += " " + flag;
    }

    @When("^user executes the command$")
    public void user_executes_the_command() throws Throwable {
        testClient.init();
        testClient.sendMessage(pasteCommand);
    }

    @Then("^user will see the paste url$")
    public void user_will_see_the_paste_url() throws Throwable {
        String result = testClient.getAllFromSocket();
        assertTrue(result.contains("Paste submitted"));
    }

    @Given("^the user has mail in the mailbox$")
    public void the_user_has_mail_in_the_mailbox() throws Throwable {
        testClient.sendMessage("login username test password 1234");
        testClient.getAllFromSocket(); // wait for login completion
        Mailbox.create();
        Mailbox.getMailbox().get().fill(MailExample.reviewExample());
    }

    @Then("^user will see the new mail$")
    public void user_will_see_the_new_mail() throws Throwable {
        String fullReply = testClient.getAllFromSocket();
        System.out.println("fullReply: " + fullReply);
        assertContains(fullReply, "There are 3 unread code reviews");
        assertContains(fullReply, "rainfall reviewed by Bossman Samu");
        assertContains(fullReply, "Keep up the good work.");
        assertContains(fullReply, "good code");
    }

    @Given("^polling for reviews is not in progress$")
    public void polling_for_reviews_is_not_in_progress() throws Throwable {
        TmcServiceScheduler.enablePolling();
        assertFalse(TmcServiceScheduler.isRunning());
        testClient.sendMessage("login username test password 1234");
    }

    @Then("^the polling will be started$")
    public void the_polling_will_be_started() throws Throwable {
        testClient.getAllFromSocket();
        assertTrue(TmcServiceScheduler.isRunning());
    }

    private void assertContains(String testedString, String expectedContent) {
        assertTrue(testedString.contains(expectedContent));
    }
}
