package fi.helsinki.cs.tmc.core.testhelpers.builders;

import fi.helsinki.cs.tmc.core.domain.Review;

import java.util.ArrayList;
import java.util.List;

public class ReviewListBuilder {
    private List<Review> exercises;

    public ReviewListBuilder() {
        this.exercises = new ArrayList<>();
    }

    public ReviewListBuilder withExercise(int id, boolean markedAsRead) {
        Review review = new Review();
        review.setId(id);
        review.setMarkedAsRead(markedAsRead);
        exercises.add(review);
        return this;
    }

    public List<Review> build() {
        return this.exercises;
    }
}
