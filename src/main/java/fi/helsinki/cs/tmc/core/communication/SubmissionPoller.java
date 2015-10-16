package fi.helsinki.cs.tmc.core.communication;

import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.domain.submission.FeedbackQuestion;
import fi.helsinki.cs.tmc.core.domain.submission.SubmissionResult;
import fi.helsinki.cs.tmc.core.domain.submission.SubmissionResultParser;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;

import com.google.common.base.Optional;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
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

    private final String timeOutmessage =
            "Something went wrong. " + "Please check your internet connection.";

    private SubmissionResult latestResult;
    private TmcApi tmcApi;
    private SubmissionResultParser submissionParser;

    /**
     * Default constuctor.
     */
    public SubmissionPoller(TmcApi tmcApi) {
        this.tmcApi = tmcApi;
        this.submissionParser = new SubmissionResultParser();
    }

    /**
     * Constructor for tests.
     */
    public SubmissionPoller(TmcApi tmcApi, int timeout) {
        this(tmcApi);
        this.timeOut = timeout;
    }

    /**
     * Returns a ready SubmissionResult with all fields complete after processing.
     *
     * @param url url to make request to
     * @return SubmissionResult containing details of submission. Null if timed out.
     * @throws InterruptedException if thread failed to sleep
     */
    private Optional<SubmissionResult> pollSubmissionUrl(URI url)
            throws InterruptedException, IOException {
        for (int i = 0; i < timeOut; i++) {
            String json = tmcApi.getRawTextFrom(url);
            if (!isProcessing(json)) {
                SubmissionResult result = submissionParser.parseFromJson(json);
                return Optional.of(result);
            }
            Thread.sleep(pollInterval);
        }
        return Optional.absent();
    }

    /**
     * Returns a ready SubmissionResult with all fields complete after processing.
     *
     * @param url url to make request to
     * @param observer {@link ProgressObserver} that is informed of the polling status
     * @return SubmissionResult containing details of submission. Null if timed out.
     * @throws InterruptedException if thread failed to sleep
     */
    private Optional<SubmissionResult> pollSubmissionUrl(URI url, ProgressObserver observer)
            throws InterruptedException, IOException {
        for (int i = 0; i < timeOut; i++) {
            String json = tmcApi.getRawTextFrom(url);
            if (!isProcessing(json, observer)) {
                SubmissionResult result = submissionParser.parseFromJson(json);
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
     * Get a new submissionResult. This will update the classes state so that calls to methods like
     * resultSummary will be based on the submissionResult fetched by this method.
     *
     * @param url the submission url
     */
    public SubmissionResult getSubmissionResult(URI url)
            throws InterruptedException, TmcCoreException, IOException {
        Optional<SubmissionResult> result = pollSubmissionUrl(url);
        if (!result.isPresent()) {
            throw new TmcCoreException("Failed to receive response to submit.");
        }
        return result.get();
    }

    /**
     * Get a new submissionResult. This will update the classes state so that calls to methods like
     * resultSummary will be based on the submissionResult fetched by this method.
     *
     * @param url the submission url
     * @param observer {@link ProgressObserver} that is informed of the polling status
     */
    public SubmissionResult getSubmissionResult(URI url, ProgressObserver observer)
            throws InterruptedException, TmcCoreException, IOException {
        Optional<SubmissionResult> result = pollSubmissionUrl(url, observer);
        if (!result.isPresent()) {
            throw new TmcCoreException("Failed to receive response to submit.");
        }
        return result.get();
    }

    private boolean isProcessing(String jsonResult) {
        if (jsonResult == null || jsonResult.trim().isEmpty()) {
            return false;
        }
        JsonElement jsonElement = new JsonParser().parse(jsonResult);
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        String status = jsonObject.get("status").getAsString();
        return status.equals("processing");
    }

    private boolean isProcessing(String jsonResult, ProgressObserver observer) {
        if (jsonResult == null || jsonResult.trim().isEmpty()) {
            return false;
        }
        JsonElement jsonElement = new JsonParser().parse(jsonResult);
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        String status = jsonObject.get("status").getAsString();
        if (status.equals("processing")) {
            String progressInfo = getProgressInfoMessage(jsonObject);
            observer.progress(progressInfo);
            return true;
        }
        return false;
    }

    private String getProgressInfoMessage(JsonObject jsonObject) {
        String message = "waiting for server.";
        try {
            int yourPlace = jsonObject.get("submissions_before_this").getAsInt() + 1;
            int queueSize = jsonObject.get("total_unprocessed").getAsInt();
            message += " Place in queue: " + yourPlace + "/" + queueSize;
        } catch (ClassCastException | NullPointerException ex) {
            System.err.println(ex);
        }
        System.out.println(message);
        return message;
    }
}
