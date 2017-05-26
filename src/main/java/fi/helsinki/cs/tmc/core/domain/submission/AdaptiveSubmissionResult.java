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
    private String error; // e.g. compile error

    @SerializedName("status")
    private SubmissionResult.Status status;

    @SerializedName("points")
    private int points;

    public SubmissionResult toSubmissionResult() {
        SubmissionResult submission = new SubmissionResult();
        submission.setError(error);
        submission.setStatus(status);
        ArrayList<String> points = new ArrayList<>();
        points.add(String.valueOf(this.points));
        submission.setPoints(points);
        return submission;
    }
}
