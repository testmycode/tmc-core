package hy.tmc.core.domain.submission;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SubmissionResult {

    @SerializedName("api_version")
    private int apiVersion;
    
    @SerializedName("all_tests_passed")
    private boolean allTestsPassed;
    
    @SerializedName("user_id")
    private int userId;
    
    private String course;
    
    @SerializedName("exercise_name")
    private String exerciseName;
    
    private String status;
    
    private String[] points;
    
    @SerializedName("processing_time")
    private int processingTime;
    
    @SerializedName("message_for_paste")
    private String messageForPaste;
    
    @SerializedName("missing_review_points")
    private String[] missingReviewPoints;
    
    @SerializedName("test_cases")
    private TestCase[] testCases;
    
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

    public Validations getValidations() {
        return validations;
    }

    public void setValidations(Validations validations) {
        this.validations = validations;
    }

    public TestCase[] getTestCases() {
        return testCases;
    }

    public void setTestCases(TestCase[] testCases) {
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String[] getPoints() {
        return points;
    }

    public void setPoints(String[] points) {
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

    public String[] getMissingReviewPoints() {
        return missingReviewPoints;
    }

    public void setMissingReviewPoints(String[] missingReviewPoints) {
        this.missingReviewPoints = missingReviewPoints;
    }

}
