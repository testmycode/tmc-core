package fi.helsinki.cs.tmc.core.domain.submission;

import fi.helsinki.cs.tmc.langs.domain.TestResult;

import java.io.Serializable;
import java.util.ArrayList;

public class AdaptiveSubmissionResult implements Serializable{

    public SubmissionResult.Status status;
    public ArrayList<TestResult> testResults = new ArrayList<>();
    public String error;

    public AdaptiveSubmissionResult() {
    }

    /*
    public AdaptiveSubmissionResult() {
        status = SubmissionResult.Status.ERROR;
        error = null;
        points = 0;
    }

    @SerializedName("error")
    private final String error; // e.g. compile error

    @SerializedName("status")
    private final SubmissionResult.Status status;

    @SerializedName("points")
    private final int points;
    */
    public SubmissionResult toSubmissionResult() {
        SubmissionResult submissionResult = new SubmissionResult();
        submissionResult.setError(error);
        submissionResult.setStatus(status);
        ArrayList<String> submissionPoints = new ArrayList<>();
        submissionPoints.add(String.valueOf("1"));
        submissionResult.setPoints(submissionPoints);
        submissionResult.setTestCases(testResults);
        return submissionResult;
    }
}
