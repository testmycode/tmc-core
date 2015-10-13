package fi.helsinki.cs.tmc.core.domain.submission;

import com.google.gson.annotations.SerializedName;
import fi.helsinki.cs.tmc.langs.abstraction.ValidationResult;


import java.util.Collections;
import java.util.List;

public class SubmissionResult {

    public enum TestResultStatus {
        ALL_FAILED,
        SOME_FAILED,
        NONE_FAILED
    }

    public enum Status {
        OK,
        FAIL,
        ERROR,
        PROCESSING
    }

    @SerializedName("api_version")
    private int apiVersion;

    @SerializedName("all_tests_passed")
    private boolean allTestsPassed;

    @SerializedName("user_id")
    private int userId;

    @SerializedName("error")
    private String error; // e.g. compile error

    private String course;

    @SerializedName("exercise_name")
    private String exerciseName;

    @SerializedName("status")
    private Status status;

    private List<String> points;

    @SerializedName("processing_time")
    private int processingTime;

    @SerializedName("message_for_paste")
    private String messageForPaste;

    @SerializedName("missing_review_points")
    private List<String> missingReviewPoints;

    @SerializedName("test_cases")
    private List<TestCase> testCases;

    @SerializedName("feedback_questions")
    private List<FeedbackQuestion> feedbackQuestions;

    @SerializedName("feedback_answer_url")
    private String feedbackAnswerUrl;

    @SerializedName("solution_url")
    private String solutionUrl;

    private Validations validations;

    private String valgrind;

    private boolean reviewed;

    @SerializedName("requests_review")
    private boolean requestsReview;

    @SerializedName("submitted_at")
    private String submittedAt;

    private ValidationResult validationResult;

    public SubmissionResult() {
        status = Status.ERROR;
        error = null;
        testCases = Collections.emptyList();
        points = Collections.emptyList();
        missingReviewPoints = Collections.emptyList();
        feedbackQuestions = Collections.emptyList();
    }

    public void setValidationResult(final ValidationResult result) {
        this.validationResult = result;
    }

    public ValidationResult getValidationResult() {
        return validationResult;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Validations getValidations() {
        return validations;
    }

    public void setValidations(Validations validations) {
        this.validations = validations;
    }

    public List<TestCase> getTestCases() {
        return testCases;
    }

    public void setTestCases(List<TestCase> testCases) {
        this.testCases = testCases;
    }

    public List<FeedbackQuestion> getFeedbackQuestions() {
        return feedbackQuestions;
    }

    public void setFeedbackQuestions(List<FeedbackQuestion> feedbackQuestions) {
        this.feedbackQuestions = feedbackQuestions;
    }

    public String getFeedbackAnswerUrl() {
        return feedbackAnswerUrl;
    }

    public void setFeedbackAnswerUrl(String feedbackAnswerUrl) {
        this.feedbackAnswerUrl = feedbackAnswerUrl;
    }

    public String getSolutionUrl() {
        return solutionUrl;
    }

    public void setSolutionUrl(String solutionUrl) {
        this.solutionUrl = solutionUrl;
    }

    public String getValgrind() {
        return valgrind;
    }

    public void setValgrind(String valgrind) {
        this.valgrind = valgrind;
    }

    public boolean isReviewed() {
        return reviewed;
    }

    public void setReviewed(boolean reviewed) {
        this.reviewed = reviewed;
    }

    public boolean isRequestsReview() {
        return requestsReview;
    }

    public void setRequestsReview(boolean requestsReview) {
        this.requestsReview = requestsReview;
    }

    public String getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(String submittedAt) {
        this.submittedAt = submittedAt;
    }

    public int getApiVersion() {
        return apiVersion;
    }

    /**
     * Returns test status.
     */
    public TestResultStatus getTestResultStatus() {

        int testsFailed = 0;

        for (TestCase test : testCases) {
            if (!test.isSuccessful()) {
                testsFailed++;
            }
        }

        if (testsFailed == testCases.size()) {
            return TestResultStatus.ALL_FAILED;
        }

        if (testsFailed != 0) {
            return TestResultStatus.SOME_FAILED;
        }

        return TestResultStatus.NONE_FAILED;
    }

    public void setApiVersion(int apiVersion) {
        this.apiVersion = apiVersion;
    }

    public boolean isAllTestsPassed() {
        return allTestsPassed;
    }

    public void setAllTestsPassed(boolean allTestsPassed) {
        this.allTestsPassed = allTestsPassed;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<String> getPoints() {
        return points;
    }

    public void setPoints(List<String> points) {
        this.points = points;
    }

    public int getProcessingTime() {
        return processingTime;
    }

    public void setProcessingTime(int processingTime) {
        this.processingTime = processingTime;
    }

    public String getMessageForPaste() {
        return messageForPaste;
    }

    public void setMessageForPaste(String messageForPaste) {
        this.messageForPaste = messageForPaste;
    }

    public List<String> getMissingReviewPoints() {
        return missingReviewPoints;
    }

    public void setMissingReviewPoints(List<String> missingReviewPoints) {
        this.missingReviewPoints = missingReviewPoints;
    }

    /**
     * Returns whether validation has failed.
     */
    public boolean validationsFailed() {
        return this.validationResult == null
                ? false
                : !this.validationResult.getValidationErrors().isEmpty();
    }

    @Override
    public String toString() {
        return "SubmissionResult{"
                + "apiVersion="
                + apiVersion
                + ", \nallTestsPassed="
                + allTestsPassed
                + ", userId="
                + userId
                + ", error="
                + error
                + ", \ncourse="
                + course
                + ", exerciseName="
                + exerciseName
                + ", status="
                + status
                + ", points="
                + points
                + ", processingTime="
                + processingTime
                + ", \nmessageForPaste="
                + messageForPaste
                + ", missingReviewPoints="
                + missingReviewPoints
                + ", testCases="
                + testCases
                + ", feedbackQuestions="
                + feedbackQuestions
                + ", feedbackAnswerUrl="
                + feedbackAnswerUrl
                + ", solutionUrl="
                + solutionUrl
                + ", validations="
                + validations
                + ", \n valgrind="
                + valgrind
                + ", reviewed="
                + reviewed
                + ", requestsReview="
                + requestsReview
                + ", submittedAt="
                + submittedAt
                + ", validationResult="
                + validationResult
                + '}';
    }
}
