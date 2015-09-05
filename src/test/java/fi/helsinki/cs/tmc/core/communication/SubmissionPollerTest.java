package fi.helsinki.cs.tmc.core.communication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fi.helsinki.cs.tmc.core.CoreTestSettings;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.domain.submission.SubmissionResult;
import fi.helsinki.cs.tmc.core.domain.submission.SubmissionResult.Status;
import fi.helsinki.cs.tmc.core.testhelpers.ExampleJson;

import java.io.IOException;
import java.net.URI;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class SubmissionPollerTest {

    private SubmissionPoller submissionPoller;
    private URI url = URI.create(
            "https://example.com/staging/submissions/1764.json?api_version=7&client=tmc_cli&client_version=1");
    private CoreTestSettings settings;
    private TmcApi tmcApi;

    @Before
    public void setup() {
        settings = new CoreTestSettings();
        settings.setUsername("chang");
        settings.setPassword("rajani");
        tmcApi = Mockito.mock(TmcApi.class);
        submissionPoller = new SubmissionPoller(tmcApi, 30);
    }

    @Test
    public void successfulSubmission() throws Exception {
        Mockito.when(tmcApi.getRawTextFrom(any(URI.class)))
                .thenReturn(ExampleJson.successfulSubmission);
        SubmissionResult output = submissionPoller.getSubmissionResult(url);
        assertFalse(output == null);
        assertEquals("2014-mooc-no-deadline", output.getCourse());
        assertEquals(Status.OK, output.getStatus());
    }

    @Test
    public void unsuccessfulSubmission() throws Exception {
        Mockito.when(tmcApi.getRawTextFrom(any(URI.class)))
                .thenReturn(ExampleJson.failedSubmission);
        SubmissionResult output = submissionPoller.getSubmissionResult(url);
        assertFalse(output == null);
        assertEquals("2014-mooc-no-deadline", output.getCourse());
        assertEquals(Status.FAIL, output.getStatus());
    }

    @Test
    public void testProgressMessages() throws Exception {
        mockPolling();
        ProgressObserver observer = mock(ProgressObserver.class);
        submissionPoller.getSubmissionResult(url, observer);

        verify(observer).progress(eq("waiting for server. Place in queue: 1/1"));
        verify(observer).progress(eq("waiting for server. Place in queue: 1/5"));
        verify(observer).progress(eq("waiting for server. Place in queue: 4/8"));
    }

    private void mockPolling() throws IOException {
        String first = ExampleJson.processingSubmission;
        String second = ExampleJson.processingSubmission.replace("1", "5");
        String placeChange = ExampleJson.processingSubmission.replace("0", "3").replace("1", "8");
        String success = ExampleJson.successfulSubmission;

        when(tmcApi.getRawTextFrom(any(URI.class)))
                .thenReturn(first, second, placeChange, success);
    }
}
