package fi.helsinki.cs.tmc.core.communication.updates;

import fi.helsinki.cs.tmc.core.communication.TmcApi;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Review;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

// TODO: relocate?
public class ReviewHandler extends UpdateHandler<Review> {

    public ReviewHandler(TmcApi tmcApi) {
        super(tmcApi);
    }

    @Override
    public List<Review> fetchFromServer(Course currentCourse)
            throws IOException, URISyntaxException {
        List<Review> currentReviews = tmcApi.getReviews(currentCourse.getReviewsUrl());
        if (currentReviews == null) {
            return new ArrayList<>();
        }
        return currentReviews;
    }

    @Override
    // TODO: isUnread
    protected boolean isNew(Review review) {
        return !review.isMarkedAsRead();
    }
}
