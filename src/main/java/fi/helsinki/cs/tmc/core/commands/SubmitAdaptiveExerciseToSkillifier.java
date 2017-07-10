package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.TmcServerCommunicationTaskFactory;
import fi.helsinki.cs.tmc.core.communication.oauth2.Oauth;
import fi.helsinki.cs.tmc.core.communication.serialization.AdaptiveSubmissionResultParser;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.domain.submission.AdaptiveSubmissionResult;
import fi.helsinki.cs.tmc.core.domain.submission.SubmissionResult;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.concurrent.Callable;

public class SubmitAdaptiveExerciseToSkillifier extends AbstractSubmissionCommand<SubmissionResult> {
    private static final Logger logger = LoggerFactory.getLogger(AbstractSubmissionCommand.class);
    private static final int DEFAULT_POLL_INTERVAL = 1000 * 2;

    private Exercise exercise;

    public SubmitAdaptiveExerciseToSkillifier(ProgressObserver observer, Exercise exercise) {
        super(observer);
        this.exercise = exercise;
    }

    @VisibleForTesting
    SubmitAdaptiveExerciseToSkillifier(
            ProgressObserver observer,
            Exercise exercise,
            TmcServerCommunicationTaskFactory tmcServerCommunicationTaskFactory) {
        super(observer, tmcServerCommunicationTaskFactory);
        this.exercise = exercise;
    }

    @Override
    public SubmissionResult call() throws TmcCoreException {
        logger.info("Submitting exercise {}", exercise.getName());
        informObserver(0, "Submitting exercise to server");

        SubmissionResult submissionResult =
                submitToSkillifier(exercise, new HashMap<String, String>());

        while (true) {
            checkInterrupt();
            try {
                Thread.sleep(DEFAULT_POLL_INTERVAL);
            } catch (InterruptedException ex) {
                logger.debug("Interrupted while sleeping", ex);
            }
            try {
                logger.debug("Checking if skillifier is done processing submission");
                Callable<String> submissionResultFetcher =
                        tmcServerCommunicationTaskFactory.getSubmissionFromSkillifierFetchTask();

                String submissionStatus = submissionResultFetcher.call();
                JsonElement submission = new JsonParser().parse(submissionStatus);
                if (isProcessing(submission)) {
                    logger.debug("Skillifier not done, sleeping for {}", DEFAULT_POLL_INTERVAL);
                    informObserver(0.3, "Waiting for response from skillifier");
                } else {
                    logger.debug("Skillifier done, parsing results");
                    informObserver(0.6, "Reading submission result");

                    AdaptiveSubmissionResultParser resultParser = new AdaptiveSubmissionResultParser();
                    AdaptiveSubmissionResult adaptiveResult = resultParser.parseFromJson(submissionStatus);
                    SubmissionResult result = adaptiveResult.toSubmissionResult();

                    logger.debug("Done parsing server response");
                    informObserver(1, "Successfully read submission results");

                    return result;
                }
            } catch (Exception ex) {
                informObserver(1, "Error while waiting for response from server");
                logger.warn("Error while updating submission status from server, continuing", ex);
            }
        }
    }


    private boolean isProcessing(JsonElement submissionStatus) {
        return submissionStatus.getAsJsonObject().get("status").getAsString().equals("PROCESSING");
    }
}
