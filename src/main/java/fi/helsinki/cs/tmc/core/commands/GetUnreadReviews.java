package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.updates.ReviewHandler;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Review;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;

import java.util.List;

/**
 * A {@link Command} for retrieving unread code reviews of a course from the TMC server.
 */
public class GetUnreadReviews extends Command<List<Review>> {

    private final Course course;
    private final ReviewHandler handler;

    /**
     * Constructs a new get unread code review command that fetches unread code review for
     * {@code course} using {@code handler}.
     */
    public GetUnreadReviews(Course course, ReviewHandler handler) {
        this.handler = handler;
        this.course = course;
    }

    /**
     * Entry point for launching this command.
     */
    @Override
    public List<Review> call() throws TmcCoreException {
        try {
            return handler.getNewObjects(course);
        } catch (Exception ex) {
            throw new TmcCoreException("Failed to fetch unread code reviews", ex);
        }
    }
}
