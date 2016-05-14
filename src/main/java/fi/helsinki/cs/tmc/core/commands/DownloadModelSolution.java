package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.domain.ProgressObserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DownloadModelSolution extends Command<Void> {

    private static final Logger logger = LoggerFactory.getLogger(DownloadModelSolution.class);

    public DownloadModelSolution(ProgressObserver observer) {
        super(observer);
    }

    @Override
    public Void call() throws Exception {
        logger.warn("Received call to unsupported action, doing nothing");
        throw new UnsupportedOperationException("Not supported before CORE MILESTONE 3");
    }
}
