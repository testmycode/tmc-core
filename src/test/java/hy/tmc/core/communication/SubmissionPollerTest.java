package hy.tmc.core.communication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import hy.tmc.core.CoreTestSettings;
import hy.tmc.core.domain.submission.SubmissionResult.Status;
import hy.tmc.core.domain.submission.SubmissionResult;
import hy.tmc.core.testhelpers.ExampleJson;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class SubmissionPollerTest {

    private SubmissionPoller submissionPoller;
    private String url =
            "https://tmc.mooc.fi/staging/submissions/1764.json?api_version=7&client=tmc_cli&client_version=1";
    private CoreTestSettings settings;
    private TmcJsonParser jsonParser;

    @Before
    public void setup() {
        settings = new CoreTestSettings();
        settings.setUsername("chang");
        settings.setPassword("rajani");
        jsonParser = Mockito.mock(TmcJsonParser.class);
        submissionPoller = new SubmissionPoller(jsonParser, 30);
    }

    @Test
    public void successfulSubmission() throws Exception {
        Mockito.when(jsonParser.getRawTextFrom(Mockito.anyString()))
                .thenReturn(ExampleJson.successfulSubmission);
        SubmissionResult output = submissionPoller.getSubmissionResult(url);
        assertFalse(output == null);
        assertEquals("2014-mooc-no-deadline", output.getCourse());
        assertEquals(Status.OK, output.getStatus());
    }

    @Test
    public void unsuccessfulSubmission() throws Exception {
        Mockito.when(jsonParser.getRawTextFrom(Mockito.anyString()))
                .thenReturn(ExampleJson.failedSubmission);
        SubmissionResult output = submissionPoller.getSubmissionResult(url);
        assertFalse(output == null);
        assertEquals("2014-mooc-no-deadline", output.getCourse());
        assertEquals(Status.FAIL, output.getStatus());
    }
}
