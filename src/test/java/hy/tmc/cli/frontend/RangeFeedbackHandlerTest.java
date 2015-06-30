package hy.tmc.cli.frontend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import hy.tmc.cli.testhelpers.FeedbackBuilder;
import hy.tmc.cli.testhelpers.FrontendStub;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class RangeFeedbackHandlerTest {

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
        builder.withBasicIntRangeQuestion();
        handler.feedback(builder.build(), "");
        handler.askQuestion();

        List<String> allLines = frontend.getAllLines();
        assertTrue(allLines.contains("how many points"));
    }

    public void answerManyQuestions() {
        builder.withBasicIntRangeQuestion()
        .withNegativeIntRange();
        handler.feedback(builder.build(), "");
        handler.askQuestion();
        assertTrue(frontend.getAllLines().contains("how many points"));
        assertFalse(frontend.getAllLines().contains("hello world"));
        handler.askQuestion();
        assertTrue(frontend.getAllLines().contains("how cold is it"));
    }

    public void checkQuestionOrder() {
        builder.withSimpleTextQuestion()
               .withBasicIntRangeQuestion()
               .withNegativeIntRange();
        handler.feedback(builder.build(), "");
        handler.askQuestion();
        assertTrue(frontend.getAllLines().contains("how many points"));
        assertFalse(frontend.getAllLines().contains("hello world"));
        handler.askQuestion();
        assertTrue(frontend.getAllLines().contains("how cold is it"));
        assertFalse(frontend.getAllLines().contains("hello world"));
        handler.askQuestion();
        assertTrue(frontend.getAllLines().contains("hello world"));
    }
    
    public void validateIntRange() {
        builder.withBasicIntRangeQuestion();
        handler.feedback(builder.build(), "");
        handler.askQuestion();
        assertEquals("0", handler.validateAnswer("-1"));
    }
    
    public void instructionMessage() {
        builder.withBasicIntRangeQuestion();
        handler.feedback(builder.build(), "");
        handler.askQuestion();
        String expected = "Please give your answer as an integer within [0..10] (inclusive)";
        assertEquals(expected, frontend.getMostRecentLine());
    }
    
    public void instructionMessageTest2() {
        builder.withNegativeIntRange();
        handler.feedback(builder.build(), "");
        handler.askQuestion();
        String expected = "Please give your answer as an integer within [-10..10] (inclusive)";
        assertEquals(expected, frontend.getMostRecentLine());
    }
}