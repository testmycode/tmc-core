
package fi.helsinki.cs.tmc.core.domain;

import com.google.gson.annotations.SerializedName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Exercise implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(Exercise.class);
    private static final DateFormat DATE_FORMAT
            = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");

    private int id;
    private String name;
    private boolean locked;

    @SerializedName("deadline_description")
    private String deadlineDescription;

    // todo make this Date?
    private String deadline;

    private String checksum;

    @SerializedName("zip_url")
    private URI zipUrl;

    @SerializedName("return_url")
    private URI returnUrl;

    /**
     * The URL the solution can be downloaded from (admins only).
     */
    @SerializedName("solution_zip_url")
    private URI solutionDownloadUrl;

    private boolean returnable;

    @SerializedName("requires_review")
    private boolean requiresReview;

    private boolean attempted;
    private boolean completed;
    private boolean reviewed;

    @SerializedName("all_review_points_given")
    private boolean allReviewPointsGiven;

    @SerializedName("memory_limit")
    private String memoryLimit;

    @SerializedName("runtime_params")
    private String[] runtimeParams;

    @SerializedName("valgrind_strategy")
    private ValgrindStrategy valgrindStrategy = ValgrindStrategy.FAIL;

    @SerializedName("code_review_requests_enabled")
    private boolean codeReviewRequestsEnabled = true;

    @SerializedName("run_tests_locally_action_enabled")
    private boolean runTestsLocallyActionEnabled = true;

    @SerializedName("exercise_submissions_url")
    private URI exerciseSubmissionsUrl;

    public Exercise() {}

    public Exercise(String name) {
        this(name, "unknown-course");
    }

    public Exercise(String name, String courseName) {
        this.name = name;
        this.courseName = courseName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null) {
            throw new NullPointerException("name was null at Exercise.setName");
        }
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty at Exercise.setName");
        }
        this.name = name;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public String getDeadlineDescription() {
        return deadlineDescription;
    }

    public void setDeadlineDescription(String deadlineDescription) {
        this.deadlineDescription = deadlineDescription;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public Date getDeadlineDate() {
        try {
            return DATE_FORMAT.parse(this.getDeadline());
        } catch (ParseException ex) {
            logger.warn("Failed to parse date {}", this.getDeadline(), ex);
            return null;
        }
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public URI getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(URI returnUrl) {
        this.returnUrl = returnUrl;
    }

    public URI getZipUrl() {
        return zipUrl;
    }

    public void setZipUrl(URI zipUrl) {
        this.zipUrl = zipUrl;
    }

    public boolean isReturnable() {
        return returnable;
    }

    public void setReturnable(boolean returnable) {
        this.returnable = returnable;
    }

    public boolean isRequiresReview() {
        return requiresReview;
    }

    public void setRequiresReview(boolean requiresReview) {
        this.requiresReview = requiresReview;
    }

    public boolean isAttempted() {
        return attempted;
    }

    public void setAttempted(boolean attempted) {
        this.attempted = attempted;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isReviewed() {
        return reviewed;
    }

    public void setReviewed(boolean reviewed) {
        this.reviewed = reviewed;
    }

    public boolean isAllReviewPointsGiven() {
        return allReviewPointsGiven;
    }

    public void setAllReviewPointsGiven(boolean allReviewPointsGiven) {
        this.allReviewPointsGiven = allReviewPointsGiven;
    }

    public String getMemoryLimit() {
        return memoryLimit;
    }

    public void setMemoryLimit(String memoryLimit) {
        this.memoryLimit = memoryLimit;
    }

    public String[] getRuntimeParams() {
        return runtimeParams != null ? runtimeParams : new String[0];
    }

    public void setRuntimeParams(String[] runtimeParams) {
        this.runtimeParams = runtimeParams;
    }

    public boolean isCodeReviewRequestsEnabled() {
        return codeReviewRequestsEnabled;
    }

    public void setCodeReviewRequestsEnabled(boolean codeReviewRequestsEnabled) {
        this.codeReviewRequestsEnabled = codeReviewRequestsEnabled;
    }

    public boolean isRunTestsLocallyActionEnabled() {
        return runTestsLocallyActionEnabled;
    }

    public void setRunTestsLocallyActionEnabled(boolean runTestsLocallyActionEnabled) {
        this.runTestsLocallyActionEnabled = runTestsLocallyActionEnabled;
    }

    public URI getExerciseSubmissionsUrl() {
        return exerciseSubmissionsUrl;
    }

    public void setExerciseSubmissionsUrl(URI exerciseSubmissionsUrl) {
        this.exerciseSubmissionsUrl = exerciseSubmissionsUrl;
    }

    private String courseName;

    // Zip contains folder for the exercise.
    public Path getExtractionTarget(Path tmcRoot) {
        return tmcRoot.resolve(Paths.get(courseName));
    }

    public Path getExerciseDirectory(Path tmcRoot) {
        return tmcRoot.resolve(Paths.get(courseName, name));
    }

    public boolean isDownloaded(Path tmcRoot) {
        return Files.exists(getExerciseDirectory(tmcRoot));
    }

    public enum ValgrindStrategy {
        @SerializedName("")
        NONE,
        @SerializedName("fail")
        FAIL
    }

    public boolean hasDeadlinePassed() {
        return hasDeadlinePassedAt(new Date());
    }

    public boolean hasDeadlinePassedAt(Date time) {
        if (time == null) {
            throw new IllegalArgumentException("Received null date as parameter");
        }
        Date deadlineDate = getDeadlineDate();
        if (deadlineDate != null) {
            return deadlineDate.getTime() < time.getTime();
        } else {
            return false;
        }
    }

    public ExerciseKey getKey() {
        return new ExerciseKey(courseName, name);
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public URI getDownloadUrl() {
        return this.zipUrl;
    }

    public void setDownloadUrl(URI downloadAddress) {
        if (downloadAddress == null) {
            throw new NullPointerException(
                    "downloadAddress was null at Exercise.setDownloadAddress");
        }
        if (downloadAddress.toString().isEmpty()) {
            throw new IllegalArgumentException(
                    "downloadAddress cannot be empty at Exercise.setDownloadAddress");
        }

        this.zipUrl = downloadAddress;
    }

    public void setSolutionDownloadUrl(URI solutionDownloadUrl) {
        this.solutionDownloadUrl = solutionDownloadUrl;
    }

    public URI getSolutionDownloadUrl() {
        return solutionDownloadUrl;
    }

    public boolean requiresReview() {
        return requiresReview;
    }

    public ValgrindStrategy getValgrindStrategy() {
        return valgrindStrategy;
    }

    @Override
    public String toString() {
        return name;
    }

    public boolean isSameExercise(Exercise other) {
        return this.getCourseName().equals(other.getCourseName())
                && this.getName().equals(other.getName());
    }
}
