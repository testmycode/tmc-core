package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.TmcServerCommunicationTaskFactory;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.domain.submission.AdaptiveSubmissionResult;
import fi.helsinki.cs.tmc.core.domain.submission.SubmissionResult;
import fi.helsinki.cs.tmc.core.exceptions.NotLoggedInException;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;
import fi.helsinki.cs.tmc.core.holders.TmcLangsHolder;
import fi.helsinki.cs.tmc.core.holders.TmcSettingsHolder;
import fi.helsinki.cs.tmc.langs.domain.NoLanguagePluginFoundException;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

abstract class AbstractSubmissionCommand<T> extends Command<T> {

    private static final Logger logger
            = LoggerFactory.getLogger(AbstractSubmissionCommand.class);

    AbstractSubmissionCommand(ProgressObserver observer) {
        super(observer);
    }

    @VisibleForTesting
    AbstractSubmissionCommand(
            ProgressObserver observer,
            TmcServerCommunicationTaskFactory tmcServerCommunicationTaskFactory) {
        super(observer, tmcServerCommunicationTaskFactory);
    }

    TmcServerCommunicationTaskFactory.SubmissionResponse submitToServer(
            Exercise exercise, Map<String, String> extraParams) throws TmcCoreException {

        informObserver(0, "Zipping project");

        Path tmcRoot = TmcSettingsHolder.get().getTmcProjectDirectory();
        Path projectPath = exercise.getExerciseDirectory(tmcRoot);

        checkInterrupt();
        logger.info("Submitting project from path {}", projectPath);

        byte[] zippedProject = compressProject(projectPath);

        extraParams.put("error_msg_locale", TmcSettingsHolder.get().getLocale().toString());

        checkInterrupt();
        informObserver(0.5, "Submitting project");
        logger.info("Submitting project to server");

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

    private byte[] compressProject(Path projectPath) throws TmcCoreException {
        byte[] zippedProject;
        try {
            zippedProject = TmcLangsHolder.get().compressProject(projectPath);
        } catch (IOException | NoLanguagePluginFoundException ex) {
            informObserver(1, "Failed to compress project");
            logger.warn("Failed to compress project", ex);
            throw new TmcCoreException("Failed to compress project", ex);
        }
        return zippedProject;
    }

    SubmissionResult submitToSkillifier(
            Exercise exercise, Map<String, String> extraParams) throws TmcCoreException {

        informObserver(0, "Zipping project");

        Path tmcRoot = TmcSettingsHolder.get().getTmcProjectDirectory();
        Path projectPath = exercise.getExerciseDirectory(tmcRoot);

        checkInterrupt();
        logger.info("Submitting project to skillifier from path {}", projectPath);

        byte[] zippedProject = compressProject(projectPath);

        extraParams.put("error_msg_locale", TmcSettingsHolder.get().getLocale().toString());

        checkInterrupt();
        informObserver(0.5, "Submitting project to skillifier");
        logger.info("Submitting project to skillifier");

        try {
            String resultJson
                    = tmcServerCommunicationTaskFactory
                    .getSubmittingExerciseToSkillifierTask(exercise, zippedProject, extraParams)
                    .call();

            informObserver(1, "Submission to skillifier successfully completed");
            logger.info("Submission to skillifier successfully completed");

            try {
                Gson gson = new GsonBuilder().create();
                SubmissionResult result = gson.fromJson(resultJson, AdaptiveSubmissionResult.class).toSubmissionResult();
                return result;
            } catch (Exception e) {
                logger.warn("Failed to parse submission result from skillifier", e);
            }
            return null;

        } catch (Exception ex) {
            if (ex instanceof NotLoggedInException) {
                throw (NotLoggedInException)ex;
            }
            informObserver(1, "Failed to submit exercise to skillifier");
            logger.warn("Failed to submit exercise to skillifier", ex);
            throw new TmcCoreException("Failed to submit exercise to skillifier", ex);
        }
    }
}
