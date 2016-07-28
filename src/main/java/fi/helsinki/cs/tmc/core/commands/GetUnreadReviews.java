package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.domain.Review;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * A {@link Command} for retrieving unread code reviews of a course from the TMC server.
 */
public class GetUnreadReviews extends Command<List<Review>> {

    private static final Logger logger = LoggerFactory.getLogger(GetUnreadReviews.class);
    private Course course;

    /**
     * Constructs a new get unread code review command that fetches unread code review for
     * {@code course} using {@code handler}.
     */
    public GetUnreadReviews(ProgressObserver observer, Course course) {
        super(observer);
        this.course = course;
    }

    /**
     * Entry point for launching this command.
     */
    @Override
    public List<Review> call() throws Exception {
        logger.info("Checking for new code reviews");
        informObserver(0, "Checking for new reviews");
        Callable<List<Review>> a = tmcServerCommunicationTaskFactory.getDownloadingReviewListTask(course);

        List<Review> reviews = a.call();

        informObserver(1, "Done checking for new reviews");
        logger.info("Checking for new reviews done");

        return reviews;
    }
}
