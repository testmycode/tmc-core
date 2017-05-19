/*
 * Author: Ohtu Summer devs 2017
 */

package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.TmcServerCommunicationTaskFactory;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.Progress;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;


import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DownloadAdaptiveExercise extends ExerciseDownloadingCommand<Exercise> {

    private static final Logger logger = LoggerFactory.getLogger(SendFeedback.class);

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
        //informObserver("MOI");
        Exercise exercise = tmcServerCommunicationTaskFactory.getAdaptiveExercise().call();
        if (exercise == null) {
            return null;
        }
        exercise.setName("AdaLovelace");
        exercise.setCourseName("None");
        //Tallennuspolku riippuu edellämainituista nimistä, polku: maindirectory/courseName/exerciseName
        byte[] zipb = tmcServerCommunicationTaskFactory.getDownloadingExerciseZipTask(exercise).call();
        System.out.println(zipb.length);
        //checkInterrupt();
        Progress progress = new Progress(3);
        extractProject(zipb, exercise, progress);
        return exercise;
    }
}
