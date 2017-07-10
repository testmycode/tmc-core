package fi.helsinki.cs.tmc.core.communication.serialization;

import static org.junit.Assert.assertEquals;

import fi.helsinki.cs.tmc.core.domain.submission.AdaptiveSubmissionResult;
import fi.helsinki.cs.tmc.core.domain.submission.SubmissionResult;

import org.junit.Test;

public class AdaptiveSubmissionResultParserTest {

    @Test
    public void skillifierResultIsParsedCorrectly() {
        String json = "{\"testResults\":[{\"name\":\"AdaLovelaceTest test\",\"successful\":false,"
                + "\"points\":[\"01-01\"],\"message\":\"Et tulostanut mitään!\","
                + "\"exception\":[\"Et tulostanut mitään!\",\"org.junit.Assert.fail(Assert.java:88)\"],"
                + "\"valgrindFailed\":false}],\"status\":\"FAIL\"}";
        AdaptiveSubmissionResultParser parser = new AdaptiveSubmissionResultParser();
        AdaptiveSubmissionResult result = parser.parseFromJson(json);
        assertEquals(SubmissionResult.Status.FAIL, result.status);
    }

}
