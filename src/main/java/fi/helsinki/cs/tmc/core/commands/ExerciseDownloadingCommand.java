package fi.helsinki.cs.tmc.core.commands;


import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.communication.TmcServerCommunicationTaskFactory;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.Progress;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.exceptions.ExerciseDownloadFailedException;
import fi.helsinki.cs.tmc.core.exceptions.ExtractingExerciseFailedException;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;
import fi.helsinki.cs.tmc.core.exceptions.TmcInterruptionException;
import fi.helsinki.cs.tmc.core.holders.TmcLangsHolder;
import fi.helsinki.cs.tmc.core.holders.TmcSettingsHolder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

abstract class ExerciseDownloadingCommand<T> extends Command<T> {

    private static final Logger logger
            = LoggerFactory.getLogger(ExerciseDownloadingCommand.class);

    public ExerciseDownloadingCommand(ProgressObserver observer) {
        super(observer);
    }

    public ExerciseDownloadingCommand(TmcSettings settings, ProgressObserver observer) {
        super(settings, observer);
    }

    public ExerciseDownloadingCommand(
            ProgressObserver observer,
            TmcServerCommunicationTaskFactory tmcServerCommunicationTaskFactory) {
        super(observer, tmcServerCommunicationTaskFactory);
    }

    public ExerciseDownloadingCommand(
                TmcSettings settings,
                ProgressObserver observer,
                TmcServerCommunicationTaskFactory tmcServerCommunicationTaskFactory) {
        super(settings, observer, tmcServerCommunicationTaskFactory);
    }

    protected byte[] downloadExercise(Exercise exercise, Progress progress) throws Exception {
        informObserver(progress.incrementAndGet(), "Downloading exercise " + exercise.getName());
        logger.info("Downloading exercise {}", exercise.getName());

        return tmcServerCommunicationTaskFactory
                .getDownloadingExerciseZipTask(exercise)
                .call();
    }

    protected void extractSolution(byte[] zip, Exercise exercise, Progress progress)
            throws TmcInterruptionException, TmcCoreException {
        logger.info("Extracting solution of exercise {}", exercise.getName());
        Path exerciseZipTemporaryPath = writeToTmp(zip, exercise, progress);
        Path target = exercise.getExtractionTarget(
                TmcSettingsHolder.get().getTmcProjectDirectory());

        logger.debug("Target path for {} is {}", exercise.getName(), target);

        try {
            TmcLangsHolder.get().extractAndRewriteEveryhing(exerciseZipTemporaryPath, target);
            logger.debug("Successfully extracted");
        } catch (Exception ex) {
            logger.warn(
                    "Failed to extract project from "
                        + exerciseZipTemporaryPath
                        + " to "
                        + target,
                    ex);
            throw new ExtractingExerciseFailedException(exercise, ex);
        } finally {
            cleanUp(exerciseZipTemporaryPath);
        }
    }

    private Path writeToTmp(byte[] zip, Exercise exercise, Progress progress)
            throws ExerciseDownloadFailedException, TmcInterruptionException {
        logger.debug("Writing zip to temporary location");
        Path exerciseZipTemporaryPath;
        try {
            exerciseZipTemporaryPath = writeToTemp(zip);
        } catch (IOException ex) {
            logger.warn("Failed to write downloaded zip to disk", ex);
            throw new ExerciseDownloadFailedException(exercise, ex);
        }

        checkInterrupt();

        logger.debug("Zip file successfully written");
        informObserver(progress.incrementAndGet(), "Extracting exercise " + exercise.getName());

        return exerciseZipTemporaryPath;
    }

    protected void extractProject(byte[] zip, Exercise exercise, Progress progress)
            throws TmcInterruptionException, TmcCoreException {
        logger.info("Extracting project");
        Path exerciseZipTemporaryPath = writeToTmp(zip, exercise, progress);
        Path target = exercise
                .getExtractionTarget(TmcSettingsHolder.get().getTmcProjectDirectory());

        try {
            TmcLangsHolder.get().extractProject(exerciseZipTemporaryPath, target);
            logger.info("Successfully extracted project");
        } catch (Exception ex) {
            logger.warn(
                    "Failed to extract project from "
                        + exerciseZipTemporaryPath
                        + " to "
                        + target,
                    ex);
            throw new ExtractingExerciseFailedException(exercise, ex);
        } finally {
            cleanUp(exerciseZipTemporaryPath);
        }
    }

    private void cleanUp(Path zip) {
        try {
            Files.deleteIfExists(zip);
            logger.debug("Cleaned up temporary files");
        } catch (IOException ex) {
            logger.warn("Failed to delete temporary exercise zip from " + zip, ex);
        }
    }

    private Path writeToTemp(byte[] zip) throws IOException {
        Path target = Files.createTempFile("tmc-exercise-", ".zip");
        Files.write(target, zip);
        return target;
    }
}
