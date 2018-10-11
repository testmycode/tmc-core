package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.TmcServerCommunicationTaskFactory;
import fi.helsinki.cs.tmc.core.communication.TmcServerCommunicationTaskFactory.SubmissionResponse;
import fi.helsinki.cs.tmc.core.communication.serialization.SubmissionResultParser;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.domain.submission.SubmissionResult;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;
import fi.helsinki.cs.tmc.core.holders.TmcSettingsHolder;

import com.google.common.annotations.VisibleForTesting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * A {@link Command} for submitting an exercise to the server.
 */
public class Submit extends AbstractSubmissionCommand<SubmissionResult> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractSubmissionCommand.class);
    private static final int DEFAULT_POLL_INTERVAL = 1000 * 2;

    private Exercise exercise;
    private Consumer<SubmissionResponse> initialSubmissionResult;

    public Submit(ProgressObserver observer, Exercise exercise) {
        super(observer);
        this.exercise = exercise;
    }

    public Submit(ProgressObserver observer, Exercise exercise, Consumer<SubmissionResponse> initialSubmissionResult) {
        super(observer);
        this.exercise = exercise;
        this.initialSubmissionResult = initialSubmissionResult;
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
        logger.info("Submitting exercise {}", exercise.getName());

        // TODO: Force send snapshots

        SubmissionResponse submissionResponse =
                submitToServer(exercise, new HashMap<String, String>());

        int pollInterval = DEFAULT_POLL_INTERVAL;
        int runtime = 0;

        try {
            Thread.sleep(pollInterval);
        } catch (InterruptedException ex) {
            logger.debug("Interrupted while sleeping", ex);
        }

        boolean initialSubmissionResultSent = false;
        while (true) {
            checkInterrupt();
            if (runtime > 10000) {
                pollInterval = 1000 * 5;
            }
            if (runtime > 30000) {
                pollInterval = 1000 * 10;
            }
            if (runtime > 300000) {
                pollInterval = 1000 * 60;
            }
            try {
                logger.debug("Checking if server is done processing submission");
                Callable<String> submissionResultFetcher =
                        tmcServerCommunicationTaskFactory.getSubmissionFetchTask(
                                submissionResponse.submissionUrl);

                String submissionStatus = submissionResultFetcher.call();
                SubmissionResult submission = new SubmissionResultParser().parseFromJson(submissionStatus);

                if (initialSubmissionResult != null && !initialSubmissionResultSent) {
                    initialSubmissionResult.accept(submissionResponse);
                    initialSubmissionResultSent = true;
                }
                if (submission.getStatus() == SubmissionResult.Status.PROCESSING) {
                    logger.debug("Server not done, sleeping for {}", pollInterval);

                    SubmissionResult.SandboxStatus sandboxStatus = submission.getSandboxStatus();

                    double percentDone = 0.0;
                    if (runtime > 120000) {
                        informObserver(percentDone,
                                "This seems to be taking a long time — "
                                        + "consider continuing to the next exercise while this is running. "
                                        + "Your submission will still be graded. "
                                        + "Check the results later at " + TmcSettingsHolder.get().getServerAddress());
                    } else if (sandboxStatus == SubmissionResult.SandboxStatus.CREATED) {
                        logger.debug("Submission received. Waiting for it to be processed.");
                        percentDone = 0.3;
                        informObserver(percentDone, "Submission received. Waiting for it to be processed.");
                    } else if (sandboxStatus == SubmissionResult.SandboxStatus.SENDING_TO_SANDBOX) {
                        logger.debug("Submission queued for processing.");
                        percentDone = 0.45;
                        informObserver(percentDone, "Submission queued for processing.");
                    } else if (sandboxStatus == SubmissionResult.SandboxStatus.PROCESSING_ON_SANDBOX) {
                        logger.debug("Testing submission.");
                        percentDone = 0.75;
                        informObserver(percentDone, "Testing submission.");
                    }

                    Thread.sleep(pollInterval);
                } else {
                    logger.debug("Server done, parsing results");
                    informObserver(1, "Processing complete.");

                    SubmissionResultParser resultParser = new SubmissionResultParser();
                    SubmissionResult result = resultParser.parseFromJson(submissionStatus);

                    logger.debug("Done parsing server response");

                    return result;
                }
            } catch (Exception ex) {
                informObserver(1, "Error while waiting for response from server");
                logger.warn("Error while updating submission status from server, continuing", ex);
            }
            runtime += pollInterval;
        }
    }
}
