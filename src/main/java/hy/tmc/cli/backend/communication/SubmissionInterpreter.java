package hy.tmc.cli.backend.communication;

import com.google.common.base.Optional;

import hy.tmc.cli.domain.submission.FeedbackQuestion;
import hy.tmc.cli.domain.submission.SubmissionResult;
import hy.tmc.cli.domain.submission.TestCase;
import hy.tmc.cli.domain.submission.ValidationError;
import hy.tmc.cli.frontend.communication.server.ProtocolException;
import hy.tmc.cli.frontend.formatters.SubmissionResultFormatter;
import java.io.IOException;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class SubmissionInterpreter {

    /**
     * Number of poll attempts. If the interval is one second, the timeout will be n seconds.
     */
    private final int timeOut = 30;

    /**
     * Milliseconds to sleep between each poll attempt.
     */
    private final int pollInterval = 1000;
    
    private final String timeOutmessage = "Something went wrong. "
            + "Please check your internet connection.";
    
    private final SubmissionResultFormatter formatter ;
    
    public SubmissionInterpreter(SubmissionResultFormatter formatter) {
        this.formatter = formatter;
    }

    private SubmissionResult latestResult;

    /**
     * Returns a ready SubmissionResult with all fields complete after
     * processing.
     *
     * @param url url to make request to
     * @return SubmissionResult containing details of submission. Null if timed out.
     * @throws InterruptedException if thread failed to sleep
     */
    private Optional<SubmissionResult> pollSubmissionUrl(String url) throws InterruptedException, IOException {
        for (int i = 0; i < timeOut; i++) {
            SubmissionResult result = TmcJsonParser.getSubmissionResult(url);
            if (result.getStatus() == null || !result.getStatus().equals("processing")) {
                return Optional.of(result);
            }
            Thread.sleep(pollInterval);
        }
        return Optional.absent();
    }

    /**
     * Returns feedback questions from the latest submission result.
     */
    public List<FeedbackQuestion> getFeedbackQuestions() {
        return latestResult.getFeedbackQuestions();
    }

    /**
     * Get a new submissionResult. This will update the classes state so that calls to methods
     * like resultSummary will be based on the submissionResult fetched by this method.
     *
     * @param url the submission url
     */
    public SubmissionResult getSubmissionResult(String url) throws InterruptedException,
            ProtocolException,
            IOException {
        Optional<SubmissionResult> result = pollSubmissionUrl(url);
        if (!result.isPresent()) {
            throw new ProtocolException("Failed to receive response to submit.");
        }
        latestResult = result.get();
        return latestResult;
    }

    /**
     * Organizes SubmissionResult into human-readable form from the URL.
     *
     */
    public String resultSummary(String url, boolean detailed) throws InterruptedException, IOException {
        Optional<SubmissionResult> result = pollSubmissionUrl(url);
        if (result.isPresent()) {
            return summarize(result.get(), detailed);
        }
        return timeOutmessage;
    }

    /**
     * Organizes SubmissionResult into human-readable form.
     *
     * @param detailed true for stack trace, always show successful.
     * @return a String containing human-readable information about tests. TimeOutMessage
    if result is null.
     * @throws InterruptedException if thread was interrupted.
     */
    public String resultSummary(boolean detailed) throws InterruptedException {
        return summarize(latestResult, detailed);
    }

    private String summarize(SubmissionResult result, boolean detailed) {
        if (result.isAllTestsPassed()) {
            return buildSuccessMessage(result, detailed);
        } else {
            return formatter.someTestsFailed()
                    + testCaseResults(result.getTestCases(), detailed)
                    + valgridErrors(result).or("")
                    + checkStyleErrors(result);
        }
    }

    private String checkStyleErrors(SubmissionResult result) {
        if (result.getValidations() == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();

        Map<String, List<ValidationError>> errors = result.getValidations().getValidationErrors();
        if (!errors.isEmpty()) {
            builder.append(formatter.someScenariosFailed());
        }

        for (Entry<String, List<ValidationError>> entry : errors.entrySet()) {
            parseValidationErrors(builder, entry);
        }
        return builder.toString();
    }

    private void parseValidationErrors(StringBuilder builder,
                                       Entry<String, List<ValidationError>> entry) {
        builder.append(formatter.parseValidationErrors(entry));
    }

    private Optional<String> valgridErrors(SubmissionResult result) {
        return Optional.of(result.getValgrind());
    }

    private String buildSuccessMessage(SubmissionResult result, boolean detailed) {
        StringBuilder builder = new StringBuilder();
        builder.append(formatter.allTestsPassed())
                .append(formatter.getPointsInformation(result))
                .append(testCaseResults(result.getTestCases(), detailed))
                .append(formatter.viewModelSolution(result.getSolutionUrl()));
        return builder.toString();
    }

    private String testCaseResults(TestCase[] cases, boolean showSuccessful) {
        StringBuilder result = new StringBuilder();
        for (TestCase testCase : cases) {
            if (showSuccessful || !testCase.isSuccessful()) {
                result.append(failOrSuccess(testCase));
            }
        }
        return result.toString();
    }

    private String failOrSuccess(TestCase testCase) {
        return formatter.testCaseDescription(testCase);
    }
}
