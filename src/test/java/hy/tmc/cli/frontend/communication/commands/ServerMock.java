package hy.tmc.cli.frontend.communication.commands;

import hy.tmc.cli.domain.submission.FeedbackQuestion;
import hy.tmc.cli.frontend.FrontendListener;

import java.util.List;

@Deprecated
public class ServerMock implements FrontendListener{

    private StringBuilder printedLines;

    public ServerMock() {
        this.printedLines = new StringBuilder();
    }
    
    @Override
    public void start() {
        
    }

    public void printLine(String line) {
        this.printedLines.append(line);
    }

    @Override
    public void feedback(List<FeedbackQuestion> feedbackQuestions, String feedbackUrl) {

    }

    public String getPrintedLine() {
        return printedLines.toString();
    }
}
