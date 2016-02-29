package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;

/**
 * A {@link Command} for retrieving unread code reviews of a course from the TMC server.
 */
public class GetUnreadReviews extends Command<Void> {


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
        throw new UnsupportedOperationException("Not supported before CORE MILESTONE 2");
    }
}
