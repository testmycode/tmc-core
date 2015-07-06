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
    
    @Test(expected = TmcCoreException.class)
    public void emptyResultFromJsonParserThrowsException() throws Exception {
        SubmissionResult result = new SubmissionResult();
        result.setStatus("processing");
        Mockito.when(jsonParser.getSubmissionResult(Mockito.anyString())).thenReturn(result);
        submissionPoller.getSubmissionResult(url);
    }

//    @Test
//    public void failedResultOutputsFailed() throws InterruptedException, IOException, ProtocolException {
//        initFailedMock();
//
//        String output = submissionInterpreter.resultSummary(url, false);
//        assertTrue(output.contains("failed"));
//
//    }
//
//    @Test
//    public void failedResultOutputContainsFailedMessages()
//            throws InterruptedException, IOException, ProtocolException {
//        initFailedMock();
//
//        String output = submissionInterpreter.resultSummary(url, false);
//        assertTrue(output.contains("et tulosta mitään!"));
//    }
//
//    @Test
//    public void succesfulResultOutputContainsPassedTestsIfDetailedOn()
//            throws InterruptedException, IOException, ProtocolException {
//        initSuccessMock();
//
//        String output = submissionInterpreter.resultSummary(url, true);
//        assertTrue(output.contains("PASSED"));
//        assertTrue(output.contains("KayttajatunnuksetTest sopivatKayvat"));
//
//    }
//
//    @Test
//    public void successfulResultOutputDoesntContainPassedTestsIfDetailedOn()
//            throws InterruptedException, IOException, ProtocolException {
//        initSuccessMock();
//
//        String output = submissionInterpreter.resultSummary(url, false);
//        assertFalse(output.contains("PASSED"));
//        assertFalse(output.contains("KayttajatunnuksetTest sopivatKayvat"));
//
//    }
//
//    @Test
//    public void resultWithCheckstyleContainsCheckstyleErrors() throws InterruptedException, IOException, ProtocolException {
//        initFailedCheckstyle();
//
//        String output = submissionInterpreter.resultSummary(url, true);
//        assertTrue(output.contains("checkstyle"));
//        assertTrue(output.contains("Class length is 478 lines (max allowed is 300)"));
//        assertTrue(output.contains("',' is not followed by whitespace."));
//    }
//
//    @Test
//    public void resultWithCheckstyleContainsLineNumberMarkings() throws InterruptedException, IOException, ProtocolException {
//        initFailedCheckstyle();
//
//        String output = submissionInterpreter.resultSummary(url, true);
//        assertTrue(output.contains("On line: 421 Column: 24"));
//        assertTrue(output.contains("On line: 202 Column: 18"));
//    }
//
//    @Test
//    public void resultWithNoCheckstyleDoesntContainCheckstyleErrors() throws InterruptedException, IOException, ProtocolException {
//        initSuccessMock();
//
//        String output = submissionInterpreter.resultSummary(url, true);
//        assertFalse(output.contains("checkstyle"));
//    }
//
//    @Test
//    public void resultWithValgridShowsValgrind() throws InterruptedException, IOException, ProtocolException {
//        initFailedValgrind();
//
//        String output = submissionInterpreter.resultSummary(url, true);
//        assertTrue(output.contains(": srunner_run_all (in /tmc/t"));
//        assertTrue(output.contains("stack size used in this run was 8388608."));
//    }
//
//    @Test
//    public void resultWithNoValgrindShowsNoValgrind() throws InterruptedException, IOException, ProtocolException {
//        initFailedMock();
//
//        String output = submissionInterpreter.resultSummary(url, true);
//        assertFalse(output.contains("Access not within mapped region at address"));
//
//    }
}
