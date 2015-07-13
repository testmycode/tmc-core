package hy.tmc.core.communication;

import com.google.common.base.Optional;

import hy.tmc.core.domain.submission.FeedbackQuestion;
import hy.tmc.core.domain.submission.SubmissionResult;
import static hy.tmc.core.domain.submission.SubmissionResult.Status.PROCESSING;
import hy.tmc.core.exceptions.TmcCoreException;
import java.io.IOException;

import java.util.List;

public class SubmissionPoller {

    /**
     * Number of poll attempts. If the interval is one second, the timeout will be n seconds.
     */
    private int timeOut = 30;

    /**
     * Milliseconds to sleep between each poll attempt.
     */
    private final int pollInterval = 1000;
    
    private final String timeOutmessage = "Something went wrong. "
            + "Please check your internet connection.";

    
    private SubmissionResult latestResult;
    private TmcJsonParser tmcJsonParser;
    
    /**
     * Default constuctor. 
     */
    public SubmissionPoller(TmcJsonParser jsonParser) {
        this.tmcJsonParser = jsonParser;
    }
    
    /**
     * Constructor for tests.
     */
    public SubmissionPoller(TmcJsonParser jsonParser, int timeout) {
        this(jsonParser);
        this.timeOut = timeout;
    }

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
            SubmissionResult result = tmcJsonParser.getSubmissionResult(url);
            if (result.getStatus() == null || result.getStatus() != PROCESSING) {
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
            TmcCoreException,
            IOException {
        Optional<SubmissionResult> result = pollSubmissionUrl(url);
        if (!result.isPresent()) {
            throw new TmcCoreException("Failed to receive response to submit.");
        }
        latestResult = result.get();
        return latestResult;
    }

}
