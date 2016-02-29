package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.domain.ProgressObserver;

/**
 * A {@link Command} for requesting a code review for code with a message.
 */
public class RequestCodeReview extends Command<Void> {

    public RequestCodeReview(ProgressObserver observer) {
        super(observer);
    }

    @Override
    public Void call() throws Exception {
        throw new UnsupportedOperationException("Not supported before CORE MILESTONE 3");
    }
}
