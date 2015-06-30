package hy.tmc.cli.backend;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;
import hy.tmc.cli.domain.Course;
import hy.tmc.cli.domain.Exercise;
import hy.tmc.cli.domain.Review;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Mailbox {

    private static Mailbox mailbox;

    private boolean newReviews;
    private boolean newUpdates;

    private List<Review> unreadReviews;
    private Map<Course, List<Exercise>> exerciseUpdates;

    private Mailbox() {
        unreadReviews = new ArrayList<>();
        exerciseUpdates = new HashMap<>();
    }

    /**
     * Fills the mailbox with reviews.
     *
     * @param reviews which contains codereviews as Review-objects
     */
    public synchronized void fill(List<Review> reviews) {
        if (mailbox == null) throw new IllegalStateException("No mailbox found.");
        newReviews = true;
        unreadReviews.addAll(reviews);
    }

    /**
     * Fills the exercise updates related to the course.
     *
     * @param course    where are updates
     * @param exercises which can be updated
     */
    @Beta
    public synchronized void fill(Course course, List<Exercise> exercises) {
        newUpdates = false;
        exercises.addAll(exerciseUpdates.get(course));
        exerciseUpdates.put(course, exercises);
    }

    /**
     * Gets all unread reviews which are automatically removed.
     *
     * @return list of code reviews
     */
    public synchronized List<Review> getUnreadReviews() {
        newReviews = false;
        List<Review> unread = new ArrayList<>(unreadReviews);
        unreadReviews.clear();
        return unread;
    }

    /**
     * Gets all exercise updates of the specific course.
     *
     * @param course to get updates from
     * @return list of exercise updates
     */
    @Beta
    public synchronized List<Exercise> getExerciseUpdates(Course course) {
        newUpdates = false;
        List<Exercise> updates = exerciseUpdates.get(course);
        exerciseUpdates.remove(course);
        return updates;
    }

    public synchronized boolean reviewsWaiting() {
        return this.newReviews;
    }

    public synchronized boolean updatesWaiting() {
        return this.newUpdates;
    }

    public static void create() {
        mailbox = new Mailbox();
    }

    public static void destroy() {
        mailbox = null;
    }

    /**
     * Empties the whole mailbox
     */
    public static void emptyMailbox() {
        Optional<Mailbox> box = getMailbox();
        if (box.isPresent()) {
            box.get().getUnreadReviews().clear();
        }
    }

    public static boolean hasMailboxInitialized() {
        return getMailbox().isPresent();
    }

    public static Optional<Mailbox> getMailbox() {
        if (mailbox == null) {
            return Optional.absent();
        } else {
            return Optional.of(mailbox);
        }
    }
}
