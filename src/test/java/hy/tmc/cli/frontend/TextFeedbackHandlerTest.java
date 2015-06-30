package hy.tmc.cli.frontend;

import hy.tmc.cli.testhelpers.FeedbackBuilder;
import hy.tmc.cli.testhelpers.FrontendStub;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class TextFeedbackHandlerTest {

    private FrontendStub frontend;
    private RangeFeedbackHandler handler;
    private FeedbackBuilder builder;

    /**
     * Make the frontend, feedbackHandler and FeedbackBuilder.
     */
    @Before
    public void setUp() throws Exception {
        this.frontend = new FrontendStub();
        this.handler = new RangeFeedbackHandler(frontend);
        this.builder = new FeedbackBuilder();
    }

    public void answerOneQuestion() {
        builder.withSimpleTextQuestion();
        handler.feedback(builder.build(), "");
        handler.askQuestion();

        List<String> allLines = frontend.getAllLines();
        assertTrue(allLines.contains("hello world"));
    }

    public void answerManyQuestions() {
        builder.withSimpleTextQuestion()
                .withLongTextQuestion();
        handler.feedback(builder.build(), "");
        handler.askQuestion();
        assertTrue(frontend.getAllLines().contains("hello world"));
        assertFalse(frontend.getAllLines().contains("how many points"));
        handler.askQuestion();
        assertTrue(frontend.getAllLines().contains("A very long strory\nblaablaablaa\n<(^)\n(___)"));
    }
}