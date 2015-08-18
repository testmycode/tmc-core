package fi.helsinki.cs.tmc.core.commands;

import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;

import fi.helsinki.cs.tmc.core.CoreTestSettings;
import fi.helsinki.cs.tmc.core.domain.Course;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

public class SendFeedbackTest {

    private static final String URL = "http://localhost:8080/feedback";

    private SendFeedback command;
    private CoreTestSettings settings;

    @Rule
    public WireMockRule wireMock = new WireMockRule();

    @Before
    public void setUp() {
        settings = new CoreTestSettings();

        command = new SendFeedback(settings, testCaseMap(), URL);
    }

    private Map<String, String> testCaseMap() {
        Map<String, String> answers = new TreeMap<>();
        answers.put("4", "jee jee!");
        answers.put(
                "13", "Oli kiva tehtävä. Opin paljon koodia, nyt tunnen osaavani paljon paremmin");
        answers.put("88", "<(^)\n (___)\n lorem ipsum, sit dolor amet");

        return answers;
    }

    @Test
    public void testCallSendsFeedbackToServer() throws Exception {
        String expected = "[{\"question_id\":\"4\", \"answer\":\"jee jee!\"},"
                + "{\"question_id\":\"13\", \"answer\":\"Oli kiva tehtävä. Opin paljon koodia, "
                + "nyt tunnen osaavani paljon paremmin\"},{\"question_id\":\"88\", "
                + "\"answer\":\"<(^)\n (___)\n lorem ipsum, sit dolor amet\"}]";

        wireMock.stubFor(
                post(urlMatching("/feedback.*"))
                        .withRequestBody(matching(expected))
                        .willReturn(
                                WireMock.aResponse()
                                        .withStatus(200)));

        Course currentCourse = new Course();
        currentCourse.setSpywareUrls(Arrays.asList("http://localhost:8080/spyware"));
        settings.setCurrentCourse(currentCourse);

        command = new SendFeedback(settings, testCaseMap(), URL);

        command.call();

        verify(postRequestedFor(urlMatching("/feedback.*")));
    }
}
