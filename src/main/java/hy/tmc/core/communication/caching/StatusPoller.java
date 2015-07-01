package hy.tmc.core.communication.caching;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.AbstractScheduledService;

import hy.tmc.core.domain.Course;
import hy.tmc.core.domain.Exercise;
import hy.tmc.core.domain.Review;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class StatusPoller extends AbstractScheduledService {

    private Course currentCourse;
    private Scheduler pollScheduler;
    private File cache;
    private ReviewHandler reviewHandler;
    private UpdateHandler updateHandler;

    /**
     * StatusPoller which polls code reviews. Has fixed time interval which can
     * be directly modified even if the polling is running.
     *
     * @param currentCourse course which code reviews are being checked
     * @param schedule object that contains the time interval
     * @param cache a file to which the polled information is saved.
     */
    public StatusPoller(Course currentCourse, Scheduler schedule, File cache) {
        this.currentCourse = currentCourse;
        this.pollScheduler = schedule;
        this.cache = cache;
        this.reviewHandler = new ReviewHandler();
        this.updateHandler = new UpdateHandler(cache);
    }

    @Override
    protected void runOneIteration() throws IOException, Exception {
        List<Review> reviews = reviewHandler.getNewObjects(currentCourse);
        List<Exercise> updates = updateHandler.getNewObjects(currentCourse);
    }

    @Override
    protected Scheduler scheduler() {
        return this.pollScheduler;
    }

    @Override
    protected void startUp() throws Exception {
    }

    @Override
    protected void shutDown() throws Exception {
    }
}
