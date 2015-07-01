package hy.tmc.core.communication.caching;

import hy.tmc.core.communication.TmcJsonParser;
import hy.tmc.core.domain.Course;
import hy.tmc.core.domain.Review;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReviewHandler extends NotificationHandler<Review> {


    public ReviewHandler() {
        super();
    }
    
    @Override
    protected List<Review> fetchFromServer(Course currentCourse) throws IOException{
        List<Review> currentReviews = TmcJsonParser.getReviews(currentCourse.getReviewsUrl());
        if (currentReviews == null) {
            return new ArrayList<>();
        }
        return currentReviews;
    }

    @Override
    protected boolean isNew(Review review) {
        return !review.isMarkedAsRead();
    }
}
