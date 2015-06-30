package hy.tmc.core.domain;

import com.google.gson.annotations.SerializedName;

public class Exercise {

    private int id; // = 284;
    private String name; //": "viikko1-Viikko1_000.Hiekkalaatikko",
    private boolean locked; // false,

    @SerializedName("deadline_description")
    private String deadlineDescription; //: null,

    // todo make this Date?
    private String deadline; //: null,

    private String checksum; //: "406f2f0690550c6dea94f319b2b1580c",

    @SerializedName("return_url")
    private String returnUrl; //: "https://tmc.mooc.fi/staging/exercises/284/submissions.json",

    @SerializedName("zip_url")
    private String zipUrl; //": "https://tmc.mooc.fi/staging/exercises/284.zip",

    private boolean returnable; //": true,

    @SerializedName("requires_review")
    private boolean requiresReview;//": false,

    private boolean attempted; //": false,
    private boolean completed; //": false,
    private boolean reviewed; //": false,

    @SerializedName("all_review_points_given")
    private boolean allReviewPointsGiven; //": true,

    @SerializedName("memory_limit")
    private String memoryLimit; //": null,

    @SerializedName("runtime_params")
    private String[] runtimeParams; //[ "-Xss8M" ]

    @SerializedName("valgrind_strategy")
    private String valgrindStrategy; // "fail",

    @SerializedName("code_review_requests_enabled")
    private boolean codeReviewRequestsEnabled; //": true,

    @SerializedName("run_tests_locally_action_enabled")
    private boolean runTestsLocallyActionEnabled; //": true,

    @SerializedName("exercise_submissions_url")
    private String exerciseSubmissionsUrl; // https://tmc.mooc.fi/staging/exercises/284.json?api_version=7

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

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public String getReturnUrlWithApiVersion() {
        if (!returnUrl.contains("api_version")) {
            return returnUrl + "?api_version=7";
        }
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    public String getZipUrl() {
        return zipUrl;
    }

    public void setZipUrl(String zipUrl) {
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
        return runtimeParams;
    }

    public void setRuntimeParams(String[] runtimeParams) {
        this.runtimeParams = runtimeParams;
    }

    public String getValgrindStrategy() {
        return valgrindStrategy;
    }

    public void setValgrindStrategy(String valgrindStrategy) {
        this.valgrindStrategy = valgrindStrategy;
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

    public String getExerciseSubmissionsUrl() {
        return exerciseSubmissionsUrl;
    }

    public void setExerciseSubmissionsUrl(String exerciseSubmissionsUrl) {
        this.exerciseSubmissionsUrl = exerciseSubmissionsUrl;
    }
}
