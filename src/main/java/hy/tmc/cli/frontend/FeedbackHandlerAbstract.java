package hy.tmc.cli.frontend;

import hy.tmc.cli.domain.submission.FeedbackQuestion;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

public abstract class FeedbackHandlerAbstract {
    protected final FrontendListener server;
    protected final Queue<FeedbackQuestion> feedbackQueue;
    protected int lastQuestionId;
    private String feedbackUrl;
    protected String lastKind;

    public FeedbackHandlerAbstract(FrontendListener server) {
        this.feedbackQueue = new ArrayDeque<>();
        this.server = server;
    }


    /**
     * Take the next feedback questions, arrange them so that text-questions are last, and ask the
     * first question.
     *
     * @param feedbackQuestions the questions that will be asked.
     * @param feedbackUrl the url where feedback answers should be sent to.
     */
    public void feedback(List<FeedbackQuestion> feedbackQuestions, String feedbackUrl) {
        this.feedbackQueue.clear();

        for (FeedbackQuestion question : feedbackQuestions) {
            if (!question.getKind().equals("text")) {
                this.feedbackQueue.add(question);
            }
        }

        for (FeedbackQuestion question : feedbackQuestions) {
            if (question.getKind().equals("text")) {
                this.feedbackQueue.add(question);
            }
        }

        this.feedbackUrl = feedbackUrl;
    }

    public boolean allQuestionsAsked() {
        return this.feedbackQueue.isEmpty();
    }

    /**
     * Take the next question, and ask it. the ID of the question will be remembered, and
     * is available through a getter.
     */
    public void askQuestion() {
        FeedbackQuestion nextQuestion = this.feedbackQueue.remove();
        lastQuestionId = nextQuestion.getId();
        lastKind = nextQuestion.getKind();
//        server.printLine(nextQuestion.getQuestion());
        String instructions = instructions(nextQuestion.getKind());
        if (!instructions.isEmpty()) {
//            server.printLine(instructions);
        }
    }

    protected abstract String instructions(String kind);

    /**
     * ID of the last question asked.
     */
    public int getLastId() {
        return this.lastQuestionId;
    }

    public String getFeedbackUrl() {
        return feedbackUrl;
    }
}
