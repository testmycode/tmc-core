/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author sakuolin
 */
public class GetAdaptiveExerciseAvailability extends Command<Boolean> {
    
    private static final Logger logger = LoggerFactory.getLogger(SendFeedback.class);

    public GetAdaptiveExerciseAvailability(ProgressObserver observer) {
        super(observer);
    }

    @Override
    public Boolean call() throws Exception {
        logger.info("Checking adaptive exercises availability");
        //informObserver()
        return tmcServerCommunicationTaskFactory.getNextJson().call();
    }
    
}
