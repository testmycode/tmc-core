package fi.helsinki.cs.tmc.core.domain.submission;

import fi.helsinki.cs.tmc.langs.domain.TestResult;

import java.io.Serializable;
import java.util.ArrayList;

public class AdaptiveSubmissionResult implements Serializable {

    public SubmissionResult.Status status;
    public ArrayList<TestResult> testResults = new ArrayList<>();
    public String error;

    public AdaptiveSubmissionResult() {
    }

    public SubmissionResult toSubmissionResult() {
        SubmissionResult submissionResult = new SubmissionResult();
        if (error == null && status == SubmissionResult.Status.ERROR) {
            error = "errored";
        }
        submissionResult.setError(error);
        submissionResult.setStatus(status);
        ArrayList<String> submissionPoints = new ArrayList<>();
        if (status == SubmissionResult.Status.OK) {
            submissionPoints.add("1");
        } else {
            submissionPoints.add("0");
        }
        submissionResult.setPoints(submissionPoints);
        submissionResult.setTestCases(testResults);
        return submissionResult;
    }
}
