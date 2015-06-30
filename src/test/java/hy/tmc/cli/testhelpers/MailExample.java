package hy.tmc.cli.testhelpers;

import hy.tmc.cli.domain.Review;

import java.util.ArrayList;
import java.util.List;

public class MailExample {

    /**
     * A simple example with three reviews.
     *
     * @return A list of example reviews.
     */
    public static List<Review> reviewExample() {
        List<Review> reviews = new ArrayList<>();
        reviews.add(makeReview("bad code", "Bossman Samu"));
        reviews.add(makeReview("good code", "The guru"));
        reviews.add(makeReview("Keep up the good work.", "waldo"));
        return reviews;
    }

    private static Review makeReview(String content, String reviewer) {
        Review review = new Review();
        review.setExerciseName("rainfall");
        review.setReviewBody(content);
        review.setReviewerName(reviewer);
        return review;
    }



}
