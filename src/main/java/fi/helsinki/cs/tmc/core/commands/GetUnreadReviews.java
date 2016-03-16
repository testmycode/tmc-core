package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link Command} for retrieving unread code reviews of a course from the TMC server.
 */
public class GetUnreadReviews extends Command<Void> {

    private static final Logger logger = LoggerFactory.getLogger(GetUnreadReviews.class);

    /**
     * Constructs a new get unread code review command that fetches unread code review for
     * {@code course} using {@code handler}.
     */
    public GetUnreadReviews(ProgressObserver observer) {
        super(observer);
    }

    /**
     * Entry point for launching this command.
     */
    @Override
    public Void call() throws TmcCoreException {
        logger.warn("Received call to unsupported action, doing nothing");
        throw new UnsupportedOperationException("Not supported before CORE MILESTONE 2");
    }
}
