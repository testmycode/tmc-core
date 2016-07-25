package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.domain.ProgressObserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link Command} for requesting a code review for code with a message.
 */
public class RequestCodeReview extends Command<Void> {

    private static final Logger logger = LoggerFactory.getLogger(RequestCodeReview.class);

    public RequestCodeReview(ProgressObserver observer) {
        super(observer);
    }

    @Override
    public Void call() throws Exception {
        informObserver(1, "Completed (nothing was done)");
        logger.warn("Received call to unsupported action, doing nothing");
        throw new UnsupportedOperationException("Not supported before CORE MILESTONE 3");
    }
}
