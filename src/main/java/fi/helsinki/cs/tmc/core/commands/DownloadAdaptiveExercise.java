package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.TmcServerCommunicationTaskFactory;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.Progress;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.holders.TmcSettingsHolder;

import com.google.common.annotations.VisibleForTesting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DownloadAdaptiveExercise extends ExerciseDownloadingCommand<Exercise> {

    private static final Logger logger = LoggerFactory.getLogger(DownloadAdaptiveExercise.class);

    public DownloadAdaptiveExercise(ProgressObserver observer) {
        super(observer);
    }

    @VisibleForTesting
    DownloadAdaptiveExercise(ProgressObserver observer, TmcServerCommunicationTaskFactory tmcServerCommunicationTaskFactory) {
        super(observer, tmcServerCommunicationTaskFactory);
    }

    @Override
    public Exercise call() throws Exception {
        logger.info("Checking adaptive exercises availability");
        Exercise exercise = tmcServerCommunicationTaskFactory.getAdaptiveExercise().call();
        if (exercise == null) {
            return null;
        }
        byte[] zipb = tmcServerCommunicationTaskFactory.getDownloadingExerciseZipTask(exercise).call();
        //checkInterrupt();
        Progress progress = new Progress(3);
        extractProject(zipb, exercise, progress);
        return exercise;
    }
}
