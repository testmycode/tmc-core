package fi.helsinki.cs.tmc.core.domain.submission;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by markovai on 24.5.2017.
 */
public class AdaptiveSubmissionResult {
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

    public SubmissionResult toSubmissionResult() {
        SubmissionResult submissionResult = new SubmissionResult();
        submissionResult.setError(error);
        submissionResult.setStatus(status);
        ArrayList<String> submissionPoints = new ArrayList<>();
        submissionPoints.add(String.valueOf(points));
        submissionResult.setPoints(submissionPoints);
        return submissionResult;
    }
}
