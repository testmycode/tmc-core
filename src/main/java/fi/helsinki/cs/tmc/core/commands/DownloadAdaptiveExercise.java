/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author sakuolin
 */
public class DownloadAdaptiveExercise extends Command<Exercise> {
    
    private static final Logger logger = LoggerFactory.getLogger(SendFeedback.class);

    public DownloadAdaptiveExercise(ProgressObserver observer) {
        super(observer);
    }

    @Override
    public Exercise call() throws Exception {
        logger.info("Checking adaptive exercises availability");
        //informObserver()
        return tmcServerCommunicationTaskFactory.getAdaptiveExercise().call();
    }
    
}
