package fi.helsinki.cs.tmc.core.domain.submission;

import com.google.gson.annotations.SerializedName;
import fi.helsinki.cs.tmc.langs.domain.TestResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    public SubmissionResult toSubmissionResult(){
        SubmissionResult s = new SubmissionResult();
        s.setError(error);
        s.setStatus(status);
        ArrayList<String> p = new ArrayList<>();
        p.add(String.valueOf(points));
        s.setPoints(p);
        return s;
    }
}
