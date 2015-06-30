
package hy.tmc.cli.frontend.formatters;

import hy.tmc.cli.domain.submission.TestCase;
import hy.tmc.cli.domain.submission.SubmissionResult;
import hy.tmc.cli.domain.submission.ValidationError;
import java.util.List;
import java.util.Map;

/**
 * SubmissionResultFormatter interface makes easier to attach new frontends to core. 
 * SubmissionResult is formatted with some class which implements this interface.
 */
public interface SubmissionResultFormatter {

    public String someTestsFailed();
    public String testCaseDescription(TestCase testCase);
    public String allTestsPassed();
    public String viewModelSolution(String solutionUrl);
    public String getPointsInformation(SubmissionResult result);
    public String someScenariosFailed();
    public String parseValidationErrors(Map.Entry<String, List<ValidationError>> entry);
}
