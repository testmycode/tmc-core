package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.TmcServerCommunicationTaskFactory;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.Progress;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.domain.Theme;

import com.google.common.annotations.VisibleForTesting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/**
 * Created by markovai on 2.6.2017.
 */
public class DownloadAdaptiveExerciseByTheme extends ExerciseDownloadingCommand<Exercise> {

    private static final Logger logger = LoggerFactory.getLogger(DownloadAdaptiveExerciseByTheme.class);

    private Theme theme;

    public DownloadAdaptiveExerciseByTheme(ProgressObserver observer, Theme theme) {
        super(observer);
        this.theme = theme;
    }

    @VisibleForTesting
    DownloadAdaptiveExerciseByTheme(
            ProgressObserver observer,
            TmcServerCommunicationTaskFactory tmcServerCommunicationTaskFactory,
            Theme theme) {
        super(observer, tmcServerCommunicationTaskFactory);
        this.theme = theme;
    }

    @Override
    public Exercise call() throws Exception {
        logger.info("Checking adaptive exercises availability by theme");
        Exercise exercise = tmcServerCommunicationTaskFactory.getAdaptiveExerciseByTheme(theme).call();
        if (exercise == null) {
            return null;
        }
        byte[] zipb = tmcServerCommunicationTaskFactory.getDownloadingAdaptiveExerciseZipTask(exercise).call();
        checkInterrupt();
        Progress progress = new Progress(3);
        extractProject(zipb, exercise, progress);
        return exercise;
    }
}
