package hy.tmc.cli.frontend;

import hy.tmc.cli.domain.submission.FeedbackQuestion;

import java.util.ArrayDeque;
import java.util.List;

public class RangeFeedbackHandler extends FeedbackHandlerAbstract {

    private int lowerbound;
    private int upperbound;

    public RangeFeedbackHandler(FrontendListener server) {
        super(server);
    }

    private void parseRange(String range) {
        String bounds = range.split("[\\[\\]]")[1];
        lowerbound = Integer.parseInt(bounds.split("\\.\\.")[0]);
        upperbound = Integer.parseInt(bounds.split("\\.\\.")[1]);
    }

    @Override
    protected String instructions(String kind) {
        String range = kind.replace("intrange", "");
        return "Please give your answer as an integer within " + range + " (inclusive)";
    }

    /**
     * Make sure an intrange answer is valid. If it's not valid return a valid answer
     * 
     * @param answer the answer given by the user
     * @return the answer if its valid, or the intranges lower bound otherwise
     */
    public String validateAnswer(String answer) {
        parseRange(lastKind);

        int ans;
        try {
            ans = Integer.parseInt(answer);
        } catch (NumberFormatException ex) {
            return "" + lowerbound;
        }
        if (ans < lowerbound || ans > upperbound) {
            return "" + lowerbound;
        }
        return answer;
    }

}
