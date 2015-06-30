package feature.feedback;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.fail;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;

import hy.tmc.cli.configuration.ClientData;
import hy.tmc.cli.configuration.ConfigHandler;
import hy.tmc.cli.frontend.RangeFeedbackHandler;
import hy.tmc.cli.frontend.communication.server.ProtocolException;
import hy.tmc.cli.frontend.communication.server.Server;
import hy.tmc.cli.testhelpers.ExampleJson;
import hy.tmc.cli.testhelpers.FrontendStub;
import hy.tmc.cli.testhelpers.TestClient;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FeedbackSteps {

    private FrontendStub frontStub;
    private RangeFeedbackHandler handler;

    private int port;

    private Thread serverThread;
    private TestClient testClient;
    private Server server;

    private ConfigHandler configHandler; // writes the test address
    private final String serverHost = "127.0.0.1";
    private int serverPort = 5050;
    private WireMockServer wireMockServer;
    private WireMock wireMock;
    private String wiremockAddress;
    private String feedbackAnswersUrl;
    private String lastReply = null;
    private List<String> output;

    /**
     * Starts the server and WireMock.
     * Also logs into the mocked server.
     */
    @Before
    public void initializeServer() throws IOException {
        configHandler = new ConfigHandler();
        wiremockAddress = "http://" + serverHost + ":" + serverPort;
        startWireMock();
        configHandler.writeServerAddress(wiremockAddress);
        System.out.println(wiremockAddress);
        frontStub = new FrontendStub();
        handler = new RangeFeedbackHandler(frontStub);
        server = new Server(handler);

        serverThread = new Thread(server);
        serverThread.start();
        port = configHandler.readPort();
        testClient = new TestClient(port);

        testClient.sendMessage("auth username test password lolxd");

        String reply = testClient.reply();

        if (reply.equals("Auth unsuccessful. Check your connection and/or credentials")) {
            fail("auth failed");
        }

        testClient = new TestClient(port);

    }

    /**
     * Configures WireMock.
     */
    private void startWireMock() {
        wireMockServer = new WireMockServer(wireMockConfig().port(serverPort));
        wireMockServer.start();
        WireMock.configureFor(serverHost, serverPort);

        String allCoursesExample = ExampleJson.allCoursesExample;
        allCoursesExample = allCoursesExample.replace(
                "https://tmc.mooc.fi/staging",
                wiremockAddress
        );

        wiremockGet("/courses.json?api_version=7", allCoursesExample);

        wiremockGet("/courses/27.json?api_version=7", ExampleJson.feedbackCourse.replace(
                "https://tmc.mooc.fi/staging/exercises/1653/submissions.json",
                wiremockAddress + "/submissions.json"
        ));

        wiremockPost("/submissions.json?" + configHandler.apiParam,
                ExampleJson.submitResponse.replace(
                "8080", serverPort + ""
        ));

        wiremockGet("/submissions/1781.json?" + configHandler.apiParam,
                ExampleJson.feedbackExample.replace(
                "https://tmc.mooc.fi/staging/submissions/1933/feedback_answers.json",
                wiremockAddress + "/feedback_answers.json"
        ));

        feedbackAnswersUrl = "/feedback_answers.json?" + configHandler.apiParam;

        wiremockPost(feedbackAnswersUrl, "{ status: \"ok\" }");

        wiremockGet("/user", "");

    }

    private void wiremockGet(final String urlToMock, final String returnBody) {
        stubFor(get(urlEqualTo(urlToMock))
                        .willReturn(aResponse()
                                        .withStatus(200)
                                        .withBody(returnBody)
                        )
        );
    }

    private void wiremockPost(final String urlToMock, final String returnBody) {
        stubFor(post(urlEqualTo(urlToMock))
                        .willReturn(aResponse()
                                        .withBody(returnBody)
                        )
        );
    }

    private String feedbackAnswer(String answer, String kind) {
        if (answer.contains(" ")) {
            return "answerQuestion kind " + kind + " answer { " + answer + " }";
        }
        return "answerQuestion kind " + kind + " answer " + answer;
    }

    @Given("^an exercise where some tests fail$")
    public void anExerciseWhereSomeTestsFail() throws IOException {
        wiremockGet("/submissions/1781.json?" + configHandler.apiParam,
                ExampleJson.failedSubmission);
    }

    @When("^the exercise is submitted$")
    public void theExerciseIsSubmitted() throws Throwable {
        sendExercise(File.separator + "testResources" + File.separator +
                "tmc-testcourse" + File.separator + "trivial");
    }

    @Then("^feedback questions will not be asked$")
    public void feedbackQuestionsWillNotBeAsked() throws IOException, InterruptedException {
        String reply = testClient.reply();
        while (reply != null && !reply.equals("fail")) {
            System.out.println("hei: " + reply);
            if (reply.contains("feedback")) {
                fail("asked for feedback, even though tests failed");
            }
            reply = testClient.reply();
        }
    }

    private List<String> checkForMessages() throws IOException {
        ArrayList<String> output = new ArrayList<String>();
        takeOutput(output);
        while (testClient.hasNewMessages()) {
            takeOutput(output);
        }
        takeOutput(output);
        return output;
    }

    private void takeOutput(ArrayList<String> output) {
        String reply = testClient.reply();
        output.add(reply);
        System.out.println("check: " + reply);
    }


    @Given("^the user has submitted a successful exercise$")
    public void theUserHasSubmittedASuccessfulExercise() throws ProtocolException, IOException {
        sendExercise(File.separator + "testResources" + File.separator +
                "tmc-testcourse" + File.separator + "trivial");
        checkForMessages();
    }

    private String sendExercise(String exercisePath) throws IOException {
        String submitCommand = "submit path ";
        String submitPath = System.getProperty("user.dir") + exercisePath;
        final String message = submitCommand + submitPath;
        testClient.sendMessage(message);
        return testClient.reply();
    }

    @When("^the user has answered all feedback questions$")
    public void theUserHasAnsweredAllFeedbackQuestions() throws IOException {
        // intrange [0..10]
        testClient = new TestClient(port);
        testClient.sendMessage(feedbackAnswer("3", "int"));
        checkForMessages();

        // intrange [10..100]
        testClient = new TestClient(port);
        testClient.sendMessage(feedbackAnswer("42", "int"));
        checkForMessages();

        // text
        testClient = new TestClient(port);
        testClient.sendMessage(feedbackAnswer("Hello world!", "text"));
        checkForMessages();
    }

    @Then("^feedback is sent to the server successfully$")
    public void feedbackIsSentToTheServerSuccessfully() throws IOException {
        verify(postRequestedFor(urlEqualTo(feedbackAnswersUrl)));
        /* .withRequestBody(equalToJson(
                "{ answers: [{ id: 30, answer: \"3\"}, { id: 31, answer: \"Hello world!\"}, { id: 32, answer: \"42\"} ]}")
        )); */
    }

    @When("^the user gives some answer that's not in the correct range$")
    public void theUserGivesSomeAnswerThatsNotInTheCorrectRange() throws IOException {
        // intrange [0..10]
        testClient = new TestClient(port);
        testClient.sendMessage(feedbackAnswer("12", "int"));
        checkForMessages();

        // intrange [10..100]
        testClient = new TestClient(port);
        testClient.sendMessage(feedbackAnswer("150", "int"));
        checkForMessages();

        // text
        testClient = new TestClient(port);
        testClient.sendMessage(feedbackAnswer("Hello world!", "text"));
        checkForMessages();
    }

    @Given("^an exercise with no feedback$")
    public void anExerciseWithNoFeedback() throws IOException {
        wiremockGet("/submissions/1781.json?" + configHandler.apiParam,
                ExampleJson.trivialNoFeedback);
        sendExercise("/testResources/tmc-testcourse/trivial");

    }

    @After
    public void closeAll() throws IOException {
        server.close();
        serverThread.interrupt();
        WireMock.reset();
        wireMockServer.stop();
        configHandler.writeServerAddress("http://tmc.mooc.fi/staging");
        ClientData.clearUserData();
    }
}
