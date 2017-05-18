/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.Progress;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author sakuolin
 */
public class DownloadAdaptiveExercise extends ExerciseDownloadingCommand<Exercise> {
    
    private static final Logger logger = LoggerFactory.getLogger(SendFeedback.class);

    public DownloadAdaptiveExercise(ProgressObserver observer) {
        super(observer);
    }

    @Override
    public Exercise call() throws Exception {
        Progress progress = new Progress(3);
        logger.info("Checking adaptive exercises availability");
        //informObserver
        Exercise exercise = 
            tmcServerCommunicationTaskFactory.getAdaptiveExercise().call();
        //ex.setName = "jotain"
        //ex.setCourseName = "Jotain
        //Tallennuspolku riippuu edellämainituista nimistä (TMCroot)
        byte[] zipb = tmcServerCommunicationTaskFactory.getDownloadingExerciseZipTask(exercise).call();
        //checkInterrupt();
        extractProject(zipb, exercise, progress);
        return exercise;
       // byte[] zip =  tmcServerCommunicationTaskFactory.getAdaptiveExercise().call();
    }
    
}
