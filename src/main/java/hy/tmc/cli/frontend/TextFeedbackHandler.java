package hy.tmc.cli.frontend;

import hy.tmc.cli.domain.submission.FeedbackQuestion;

public class TextFeedbackHandler extends FeedbackHandlerAbstract {

    public TextFeedbackHandler(FrontendListener server) {
        super(server);
    }

    @Override
    protected String instructions(String kind) {
        return "text";
    }

}
