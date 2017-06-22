package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.TmcServerCommunicationTaskFactory;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.Progress;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;

import com.google.common.annotations.VisibleForTesting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by markovai on 2.6.2017.
 */
public class DownloadAdaptiveExercise extends ExerciseDownloadingCommand<Exercise> {

    private static final Logger logger = LoggerFactory.getLogger(DownloadAdaptiveExercise.class);

    private int week;
    private Course course;

    public DownloadAdaptiveExercise(ProgressObserver observer, int week, Course course) {
        super(observer);
        this.week = week;
        this.course = course;
    }

    @VisibleForTesting
    DownloadAdaptiveExercise(
            ProgressObserver observer,
            TmcServerCommunicationTaskFactory tmcServerCommunicationTaskFactory,
            int week, Course course) {
        super(observer, tmcServerCommunicationTaskFactory);
        this.week = week;
        this.course = course;
    }

    @Override
    public Exercise call() throws Exception {
        logger.info("Checking adaptive exercises availability by week");
        Exercise exercise = tmcServerCommunicationTaskFactory.getAdaptiveExercise(week, course).call();
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
