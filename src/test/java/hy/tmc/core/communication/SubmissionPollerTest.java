package hy.tmc.core.communication;

import hy.tmc.core.domain.submission.SubmissionResult;
import hy.tmc.core.exceptions.TmcCoreException;
import hy.tmc.core.testhelpers.ClientTmcSettings;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class SubmissionPollerTest {

    private SubmissionPoller submissionPoller;
    private String url = "https://tmc.mooc.fi/staging/submissions/1764.json?api_version=7";
    private ClientTmcSettings settings;
    private TmcJsonParser jsonParser;

    @Before
    public void setup() {
        settings = new ClientTmcSettings();
        settings.setUsername("chang");
        settings.setPassword("rajani");
        jsonParser = Mockito.mock(TmcJsonParser.class);
        submissionPoller = new SubmissionPoller(jsonParser, 1);
    }

    @Test
    public void jsonParserIsCalled() throws Exception {
        SubmissionResult result = new SubmissionResult();
        result.setApiVersion(7);
        result.setSubmittedAt("asdljasdjalsd");
        Mockito.when(jsonParser.getSubmissionResult(Mockito.anyString())).thenReturn(result);
        SubmissionResult output = submissionPoller.getSubmissionResult(url);
        assertEquals(output, result);
    }
    
}
