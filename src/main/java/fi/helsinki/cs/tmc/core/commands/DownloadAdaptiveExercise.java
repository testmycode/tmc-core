/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.serialization.AdaptiveExerciseParser;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.Progress;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author sakuolin
 */
public class DownloadAdaptiveExercise extends ExerciseDownloadingCommand<Object> {
    
    private static final Logger logger = LoggerFactory.getLogger(SendFeedback.class);

    public DownloadAdaptiveExercise(ProgressObserver observer) {
        super(observer);
    }

    @Override
    public Exercise call() throws Exception {
        Progress progress = new Progress(3);
        logger.info("Checking adaptive exercises availability");
        //informObserver
        String json = tmcServerCommunicationTaskFactory.getJsonString().call();
        AdaptiveExerciseParser aparser = new AdaptiveExerciseParser();
        Exercise ex = aparser.parseFromJson(json);
        //ex.setName = "jotain"
        //ex.setCourseName = "Jotain
        //Tallennuspolku riippuu edellämainituista nimistä (TMCroot)
        byte[] zipb = tmcServerCommunicationTaskFactory.getDownloadingExerciseZipTask(ex).call();
        //checkInterrupt();
        extractProject(zipb, ex, progress);
        return ex;
       // byte[] zip =  tmcServerCommunicationTaskFactory.getAdaptiveExercise().call();
    }
    
}
