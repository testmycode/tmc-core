package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.TmcServerCommunicationTaskFactory;
import fi.helsinki.cs.tmc.core.communication.serialization.SubmissionResultParser;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.domain.submission.SubmissionResult;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.concurrent.Callable;

/**
 * A {@link Command} for submitting an exercise to the server.
 */
public class Submit extends AbstractSubmissionCommand<SubmissionResult> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractSubmissionCommand.class);
    private static final int DEFAULT_POLL_INTERVAL = 1000 * 2;

    private Exercise exercise;

    public Submit(ProgressObserver observer, Exercise exercise) {
        super(observer);
        this.exercise = exercise;
    }

    @VisibleForTesting
    Submit(
            ProgressObserver observer,
            Exercise exercise,
            TmcServerCommunicationTaskFactory tmcServerCommunicationTaskFactory) {
        super(observer, tmcServerCommunicationTaskFactory);
        this.exercise = exercise;
    }

    /**
     * Entry point for launching this command.
     */
    @Override
    public SubmissionResult call() throws TmcCoreException {

        TmcServerCommunicationTaskFactory.SubmissionResponse submissionResponse =
                submitToServer(exercise, new HashMap<String, String>());

        while (true) {
            checkInterrupt();
            try {
                Thread.sleep(DEFAULT_POLL_INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                Callable<String> submissionResultFetcher =
                    tmcServerCommunicationTaskFactory
                        .getSubmissionFetchTask(submissionResponse.submissionUrl.toString());


                String submissionStatus = submissionResultFetcher.call();
                JsonElement submission = new JsonParser().parse(submissionStatus);
                if (isProcessing(submission)) {
                    // TODO: Update progress
                    // TODO: Replace with variable interval polling
                    Thread.sleep(DEFAULT_POLL_INTERVAL);
                } else {
                    SubmissionResultParser resultParser = new SubmissionResultParser();
                    return resultParser.parseFromJson(submissionStatus);
                }
            } catch (Exception ex) {
                logger.warn("Error while updating submission status from server, continuing", ex);
            }
        }
    }

    private boolean isProcessing(JsonElement submissionStatus) {

        return submissionStatus.getAsJsonObject().get("status").getAsString().equals("processing");
    }
}
