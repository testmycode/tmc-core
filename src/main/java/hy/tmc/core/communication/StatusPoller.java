package hy.tmc.core.communication;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.AbstractScheduledService;

import hy.tmc.core.Mailbox;
import hy.tmc.core.domain.Course;
import hy.tmc.core.domain.Review;
import hy.tmc.core.exceptions.ProtocolException;
import hy.tmc.core.synchronization.PollScheduler;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

public class StatusPoller extends AbstractScheduledService {

    private Course currentCourse;
    private PollScheduler pollScheduler;

    /**
     * StatusPoller which polls code reviews. Has fixed time interval
     * which can be directly modified even if the polling is running.
     * @param currentCourse course which code reviews are being checked
     * @param schedule object that contains the time interval
     */
    public StatusPoller(Course currentCourse, PollScheduler schedule) {
        this.currentCourse = currentCourse;
        this.pollScheduler = schedule;
    }

    @Override
    protected void runOneIteration() throws IOException {
        if (!Mailbox.hasMailboxInitialized()) {
            throw new IllegalStateException("No mailbox initialized.");
        }

        Optional<List<Review>> reviews = checkReviews();
        if (reviews.isPresent()) {
            Mailbox.emptyMailbox(); // TODO poll only new mails without clearing all
            Mailbox.getMailbox().get().fill(reviews.get());
        }
    }

    @Override
    protected Scheduler scheduler() {
        return this.pollScheduler;
    }

    private Optional<List<Review>> checkReviews() throws IOException {
        List<Review> currentReviews = TmcJsonParser.getReviews(this.currentCourse.getReviewsUrl());
        currentReviews = filter(currentReviews);

        if (currentReviews.isEmpty()) {
            return Optional.absent();
        }
        return Optional.of(currentReviews);
    }

    private List<Review> filter(List<Review> currentReviews) {
        List<Review> filtered = new ArrayList<>();
        for (Review review : currentReviews) {
            if (!review.isMarkedAsRead()) {
                filtered.add(review);
            }
        }
        return filtered;
    }

    @Override
    protected void startUp() throws Exception {
    }

    @Override
    protected void shutDown() throws Exception {
    }
}
