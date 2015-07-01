package hy.tmc.core.commands;

import hy.tmc.core.communication.updates.ReviewHandler;
import hy.tmc.core.domain.Review;
import hy.tmc.core.exceptions.ProtocolException;
import java.io.IOException;
import java.util.List;


public class GetUnreadReviews extends Command<List<Review>>{

    private ReviewHandler handler;
    
    public GetUnreadReviews(ReviewHandler handler) {
        super();
    }
    
    @Override
    public void checkData() throws ProtocolException, IOException {
    }

    @Override
    public List<Review> call() throws Exception {
        return null;
    }

}
