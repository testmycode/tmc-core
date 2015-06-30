package hy.tmc.core.communication;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.AbstractScheduledService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import hy.tmc.core.domain.Course;
import hy.tmc.core.domain.Exercise;
import hy.tmc.core.domain.Review;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.List;

public class StatusPoller extends AbstractScheduledService {

    private Course currentCourse;
    private Scheduler pollScheduler;
    private File database;

    /**
     * StatusPoller which polls code reviews. Has fixed time interval which can
     * be directly modified even if the polling is running.
     *
     * @param currentCourse course which code reviews are being checked
     * @param schedule object that contains the time interval
     * @param database a file to which the polled information is saved.
     */
    public StatusPoller(Course currentCourse, Scheduler schedule, File database) {
        this.currentCourse = currentCourse;
        this.pollScheduler = schedule;
        this.database = database;
    }

    @Override
    protected void runOneIteration() throws IOException {
        Optional<List<Review>> reviews = checkReviews();
        if (reviews.isPresent()) {
            saveReviews(reviews.get());
        }
        Optional<List<Exercise>> updates = checkUpdates(currentCourse);
    }

    @Override
    protected Scheduler scheduler() {
        return this.pollScheduler;
    }

    private Optional<List<Review>> checkReviews() throws IOException {
        List<Review> currentReviews = TmcJsonParser.getReviews(this.currentCourse.getReviewsUrl());
        currentReviews = filterReviews(currentReviews);

        if (currentReviews.isEmpty()) {
            return Optional.absent();
        }
        return Optional.of(currentReviews);
    }

    private Optional<List<Exercise>> checkUpdates(Course currentCourse) throws IOException {
        List<Exercise> exercises = TmcJsonParser.getExercises(currentCourse);
        exercises = getUpdated(exercises);
        if (exercises.isEmpty()) {
            return Optional.absent();
        }
        return Optional.of(exercises);
    }

    private List<Review> filterReviews(List<Review> currentReviews) {
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

    private void saveReviews(List<Review> reviews) throws FileNotFoundException {
        String reviewsAsJson = new Gson().toJson(reviews);
        try (PrintWriter writer = new PrintWriter(this.database)) {
            writer.write(reviewsAsJson);
        }
    }

    private void saveUpdates(List<Exercise> exercises) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private List<Exercise> getUpdated(List<Exercise> exercises) {
        List<Exercise> updates = new ArrayList<>();
        if (exercises == null) {
            return updates;
        }
        for (Exercise exercise : exercises) {
            if (isUpdated(exercise)) {
                updates.add(exercise);
            }
        }
        return updates;
    }

    private boolean isUpdated(Exercise exercise) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
