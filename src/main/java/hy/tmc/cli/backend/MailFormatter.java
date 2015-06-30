package hy.tmc.cli.backend;

import com.google.common.annotations.Beta;
import hy.tmc.cli.domain.Review;

import java.util.List;


public class MailFormatter {

    /**
     * Formats the list of reviews as user friendly string.
     * @param reviews to be formatted
     * @return formatted string which contains reviews
     */
    public static String formatReviews(List<Review> reviews) {
        return reviewOutput(reviews);
    }
    
    private static String reviewOutput(List<Review> reviews) {
        StringBuilder builder = new StringBuilder();
        builder.append("There are ")
                .append(reviews.size())
                .append(" unread code reviews\n");
        for (Review review : reviews) {
            addReviewToOutput(review, builder);
        }
        return builder.toString();
    }

    private static void addReviewToOutput(Review review, StringBuilder builder) {
        builder.append(review.toString())
                .append("\n");
    }
}
