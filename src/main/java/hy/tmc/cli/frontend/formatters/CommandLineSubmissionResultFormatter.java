package hy.tmc.cli.frontend.formatters;

import static hy.tmc.cli.frontend.ColorFormatter.coloredString;
import static hy.tmc.cli.frontend.CommandLineColor.YELLOW;

import hy.tmc.cli.domain.submission.SubmissionResult;
import hy.tmc.cli.domain.submission.TestCase;
import hy.tmc.cli.domain.submission.ValidationError;
import static hy.tmc.cli.frontend.CommandLineColor.BLUE;
import static hy.tmc.cli.frontend.CommandLineColor.GREEN;
import static hy.tmc.cli.frontend.CommandLineColor.RED;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

/**
 * CommandLineSubmissionResultFormatter gives submissionresult explainings for command line user
 * interface. ResultInterpreter class uses this class.
 */
public class CommandLineSubmissionResultFormatter implements SubmissionResultFormatter {

    @Override
    public String someTestsFailed() {
        return "Some tests failed on server. Summary: \n";
    }

    /**
     * Tells if TestCase object is passed or failed, if failed, also information about failures.
     *
     * @param testCase
     * @return toString of StrinBuilder content
     */
    @Override
    public String testCaseDescription(TestCase testCase) {
        StringBuilder destination = new StringBuilder();
        if (testCase.isSuccessful()) {
            destination.append(coloredString(" PASSED: ", GREEN))
                    .append(testCase.getName())
                    .append("\n");
            return destination.toString();
        }
        destination.append(coloredString(" FAILED: ", RED))
                .append(testCase.getName())
                .append(" ")
                .append(testCase.getMessage())
                .append("\n");
        return destination.toString();
    }

    @Override
    public String allTestsPassed() {
        return coloredString("All tests passed. Points awarded: ", GREEN);
    }

    /**
     * Gives information about model solution.
     */
    @Override
    public String viewModelSolution(String solutionUrl) {
        return "View model solution: \n" + coloredString(solutionUrl, BLUE);
    }

    /**
     * Gives information about result points.
     */
    @Override
    public String getPointsInformation(SubmissionResult result) {
        return Arrays.toString(result.getPoints()) + "\n";
    }

    @Override
    public String someScenariosFailed() {
        return coloredString("Some checkstyle scenarios failed.", YELLOW);
    }

    @Override
    public String parseValidationErrors(Entry<String, List<ValidationError>> entry) {
        StringBuilder builder = new StringBuilder();
        builder.append("\nFile: ").append(entry.getKey());
        for (ValidationError error : entry.getValue()) {
            String errorLine = "\n  On line: " + error.getLine() + " Column: " + error.getColumn();
            builder.append(coloredString(errorLine, YELLOW));
            builder.append("\n    ").append(error.getMessage());
        }
        return builder.toString();
    }
}
