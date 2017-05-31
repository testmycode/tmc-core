package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.TmcServerCommunicationTaskFactory;
import fi.helsinki.cs.tmc.core.domain.AdaptiveExercise;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.domain.submission.AdaptiveSubmissionResult;
import fi.helsinki.cs.tmc.core.domain.submission.SubmissionResult;
import fi.helsinki.cs.tmc.core.holders.TmcSettingsHolder;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

public class SubmitAdaptiveExerciseToSkillifier extends AbstractSubmissionCommand<SubmissionResult> {
    private static final Logger logger = LoggerFactory.getLogger(AbstractSubmissionCommand.class);

    private Exercise exercise;

    public SubmitAdaptiveExerciseToSkillifier(ProgressObserver observer, String exerciseName) {
        super(observer);
        this.exercise = new AdaptiveExercise();
        exercise.setName(exerciseName);
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
    public SubmissionResult call() {
        logger.info("Submitting exercise {}", exercise.getName());
        informObserver(0, "Submitting exercise to server");
        URI submissionUrl = tmcServerCommunicationTaskFactory.getSkillifierUrl(exercise.getName()
                + "/submit?username=" + TmcSettingsHolder.get().getToken().get());
        logger.info("submissionurl: {}", submissionUrl.toString());
        String networkResult = "";
        try {
            networkResult = tmcServerCommunicationTaskFactory.getSubmissionFetchTask(submissionUrl).call();
            logger.info("network result: {}", networkResult);
        } catch (Exception e) {
            informObserver(1, "Error while waiting for response from server");
            logger.warn("Error while updating adaptive submission status from server.", e);
        }

        Gson gson = new GsonBuilder().create();
        SubmissionResult result = gson.fromJson(networkResult, AdaptiveSubmissionResult.class).toSubmissionResult();
        if (!"null".equals(result.getError())) {
            logger.warn("Submission result error: " + result.getError());
        }
        return result;
    }
}
