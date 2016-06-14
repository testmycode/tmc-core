package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.TmcServerCommunicationTaskFactory;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.Progress;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.exceptions.ExerciseDownloadFailedException;
import fi.helsinki.cs.tmc.core.exceptions.ExtractingExericeFailedException;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;
import fi.helsinki.cs.tmc.core.exceptions.TmcInterruptionException;
import fi.helsinki.cs.tmc.core.holders.TmcLangsHolder;
import fi.helsinki.cs.tmc.core.holders.TmcSettingsHolder;

import com.google.common.annotations.VisibleForTesting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A {@link Command} for downloading exercises.
 */
public class DownloadOrUpdateExercises extends ExerciseDownloadingCommand<List<Exercise>> {

    private static final Logger logger = LoggerFactory.getLogger(DownloadOrUpdateExercises.class);

    private List<Exercise> exercises;

    public DownloadOrUpdateExercises(ProgressObserver observer, List<Exercise> exercises) {
        super(observer);
        this.exercises = exercises;
    }

    @VisibleForTesting
    DownloadOrUpdateExercises(
        ProgressObserver observer,
        List<Exercise> exercises,
        TmcServerCommunicationTaskFactory tmcServerCommunicationTaskFactory) {
        super(observer, tmcServerCommunicationTaskFactory);
        this.exercises = exercises;
    }

    @Override
    public List<Exercise> call() throws TmcInterruptionException {
        List<Exercise> successfullyDownloaded = new ArrayList<>();

        /*
         * 3 states per exercise,
         * 1) download zip
         * 2) extract zip
         * 3) done
         */
        Progress progress = new Progress(exercises.size() * 3.0);
        for (Exercise exercise : exercises) {

            //TODO: Multi-thread?

            checkInterrupt();

            byte[] zip;
            try {
                zip = downloadExercise(exercise, progress);
            } catch (Exception ex) {
                logger.warn("Failed to download project from TMC-server", ex);
                continue;
            }

            checkInterrupt();

            try {
                extractProject(zip, exercise, progress);
            } catch (TmcCoreException e) {
                logger.warn("Extracting project failed", e);
                continue;
            }

            successfullyDownloaded.add(exercise);
            informObserver(progress.incrementAndGet(), "Downloaded exercise " + exercise.getName());

            //TODO: Update PluginState

            //TODO: Make into future / callable / something?
        }
        return successfullyDownloaded;
    }

}
