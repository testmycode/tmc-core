package hy.tmc.cli.testhelpers;

import hy.tmc.cli.domain.submission.FeedbackQuestion;
import hy.tmc.cli.frontend.FrontendListener;
import java.util.ArrayList;

import java.util.List;

@Deprecated
public class FrontendStub implements FrontendListener {
    
    String line;
    List<String> allLines;

    public FrontendStub() {
        allLines = new ArrayList<>();
    }
    
    @Override
    public void start() {
        
    }

    public void printLine(String line) {
        this.line = line;
        allLines.add(line);
    }

    @Override
    public void feedback(List<FeedbackQuestion> feedbackQuestions, String feedbackUrl) {

    }

    public String getMostRecentLine() {
        return line;
    }
    
    public List<String> getAllLines() {
        return this.allLines;
    }
    
}
