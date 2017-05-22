package fi.helsinki.cs.tmc.core.commands;

import com.google.common.annotations.VisibleForTesting;
import fi.helsinki.cs.tmc.core.communication.TmcServerCommunicationTaskFactory;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.domain.submission.SubmissionResult;
import fi.helsinki.cs.tmc.core.exceptions.NotLoggedInException;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;
import fi.helsinki.cs.tmc.core.exceptions.TmcInterruptionException;
import fi.helsinki.cs.tmc.core.holders.TmcLangsHolder;
import fi.helsinki.cs.tmc.core.holders.TmcSettingsHolder;
import fi.helsinki.cs.tmc.langs.domain.NoLanguagePluginFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.concurrent.Callable;

/**
 * Created by markovai on 22.5.2017.
 */
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
    public SubmissionResult call() throws Exception {
        logger.info("Submitting exercise {}", exercise.getName());
        informObserver(0, "Submitting exercise to server");

        TmcServerCommunicationTaskFactory.SubmissionResponse submissionResponse =
            submitToSkillifier(exercise, new HashMap<String, String>());
        return null;
    }

    private TmcServerCommunicationTaskFactory.SubmissionResponse submitToSkillifier(
            Exercise exercise, HashMap<String, String> extraParams) throws TmcCoreException {

        byte[] zippedProject;

        informObserver(0, "Zipping project.");
        Path tmcRoot = TmcSettingsHolder.get().getTmcProjectDirectory();
        Path projectPath = exercise.getExerciseDirectory(tmcRoot);

        //tarkista serveri riippuvaisuus
        checkInterrupt();
        logger.info("Submitting adaptive project to path {}", projectPath);

        try {
            zippedProject = TmcLangsHolder.get().compressProject(projectPath);
        } catch (IOException | NoLanguagePluginFoundException ex) {
            informObserver(1, "Failed to compress adaptive project");
            logger.warn("Failed to compress adaptive project", ex);
            throw new TmcCoreException("Failed to compress adaptive project", ex);
        }

        extraParams.put("error_msg_locale", TmcSettingsHolder.get().getLocale().toString());

        checkInterrupt();
        informObserver(0.5, "Submitting adaptive project");
        logger.info("Submitting adaptive project to skillifier");

        try {
            TmcServerCommunicationTaskFactory.SubmissionResponse response
                = tmcServerCommunicationTaskFactory
                .getSubmittingExerciseTask(exercise, zippedProject, extraParams)
                .call();

            informObserver(1, "Submission successfully completed");
            logger.info("Submission successfully completed");

            return response;
        } catch (Exception ex) {
            if (ex instanceof NotLoggedInException) {
                throw (NotLoggedInException)ex;
            }
            informObserver(1, "Failed to submit exercise");
            logger.warn("Failed to submit exercise", ex);
            throw new TmcCoreException("Failed to submit exercise", ex);
        }
    }
}
