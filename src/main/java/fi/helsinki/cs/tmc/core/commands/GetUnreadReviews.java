package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.updates.ReviewHandler;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Review;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;

import java.util.List;

public class GetUnreadReviews extends Command<List<Review>> {

    private final Course course;
    private final ReviewHandler handler;

    public GetUnreadReviews(Course course, ReviewHandler handler, TmcSettings settings) {
        super(settings);
        this.handler = handler;
        this.course = course;
    }

    @Override
    public void checkData() throws TmcCoreException {
        if (handler == null) {
            throw new TmcCoreException("reviewHandler not given");
        }
    }

    @Override
    public List<Review> call() throws Exception {
        return handler.getNewObjects(course);
    }
}
