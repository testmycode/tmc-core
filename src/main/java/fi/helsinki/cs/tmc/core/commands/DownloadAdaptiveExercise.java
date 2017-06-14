package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.TmcServerCommunicationTaskFactory;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.Progress;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.domain.Theme;

import com.google.common.annotations.VisibleForTesting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by markovai on 2.6.2017.
 */
public class DownloadAdaptiveExercise extends ExerciseDownloadingCommand<Exercise> {

    private static final Logger logger = LoggerFactory.getLogger(DownloadAdaptiveExercise.class);

    private Theme theme;
    private Course course;

    public DownloadAdaptiveExercise(ProgressObserver observer, Theme theme, Course course) {
        super(observer);
        this.theme = theme;
        this.course = course;
    }

    @VisibleForTesting
    DownloadAdaptiveExercise(
            ProgressObserver observer,
            TmcServerCommunicationTaskFactory tmcServerCommunicationTaskFactory,
            Theme theme, Course course) {
        super(observer, tmcServerCommunicationTaskFactory);
        this.theme = theme;
        this.course = course;
    }

    @Override
    public Exercise call() throws Exception {
        logger.info("Checking adaptive exercises availability by theme");
        Exercise exercise = tmcServerCommunicationTaskFactory.getAdaptiveExercise(theme, course).call();
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
