package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.TmcServerCommunicationTaskFactory;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.Progress;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;

import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

public class DownloadModelSolution extends ExerciseDownloadingCommand<Exercise> {

    private static final Logger logger = LoggerFactory.getLogger(DownloadModelSolution.class);

    private Exercise exercise;

    public DownloadModelSolution(ProgressObserver observer, Exercise exercise) {
        super(observer);
        this.exercise = exercise;
    }

    @VisibleForTesting
    DownloadModelSolution(ProgressObserver observer,
                          TmcServerCommunicationTaskFactory tmcServerCommunicationTaskFactory, Exercise exercise) {
        super(observer, tmcServerCommunicationTaskFactory);
        this.exercise = exercise;
    }

    @Override
    public Exercise call() throws Exception {
        Progress progress = new Progress(3);
        Callable<byte[]> downloadingExerciseSolutionZipTask = tmcServerCommunicationTaskFactory.getDownloadingExerciseSolutionZipTask(exercise);
        byte[] zip = downloadingExerciseSolutionZipTask.call();
        extractProject(zip, exercise, progress);
        return exercise;
    }
}
