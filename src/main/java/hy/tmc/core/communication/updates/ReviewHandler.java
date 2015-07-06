package hy.tmc.core.communication.updates;

import hy.tmc.core.communication.TmcJsonParser;
import hy.tmc.core.domain.Course;
import hy.tmc.core.domain.Review;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReviewHandler extends UpdateHandler<Review> {


    public ReviewHandler(TmcJsonParser jsonParser) {
        super(jsonParser);
    }
    
    @Override
    public List<Review> fetchFromServer(Course currentCourse) throws IOException{
        List<Review> currentReviews = jsonParser.getReviews(currentCourse.getReviewsUrl());
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
