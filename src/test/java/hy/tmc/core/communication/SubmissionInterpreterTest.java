package hy.tmc.core.communication;

import hy.tmc.core.configuration.ClientTmcSettings;
import hy.tmc.core.exceptions.TmcCoreException;
import hy.tmc.core.testhelpers.ExampleJson;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(UrlCommunicator.class)
public class SubmissionInterpreterTest {

    SubmissionPoller submissionInterpreter;
    String url = "https://tmc.mooc.fi/staging/submissions/1764.json?api_version=7";
    ClientTmcSettings settings;
    
    @Before
    public void setup() {
        settings = new ClientTmcSettings();
        PowerMockito.mockStatic(UrlCommunicator.class);

        settings.setUsername("chang");
        settings.setPassword("rajani");

        submissionInterpreter = new SubmissionPoller();
    }

    private void initFailedMock() throws IOException, TmcCoreException {
        HttpResult fakeResult = new HttpResult(ExampleJson.failedSubmission, 200, true);
        PowerMockito
                .when(UrlCommunicator.makeGetRequest(Mockito.anyString(),
                                Mockito.anyString()))
                .thenReturn(fakeResult);
    }

    private void initSuccessMock() throws IOException, TmcCoreException {
        HttpResult fakeResult = new HttpResult(ExampleJson.successfulSubmission, 200, true);
        PowerMockito
                .when(UrlCommunicator.makeGetRequest(Mockito.anyString(),
                                Mockito.anyString()))
                .thenReturn(fakeResult);
    }

    private void initFailedCheckstyle() throws IOException, TmcCoreException {
        HttpResult fakeResult = new HttpResult(ExampleJson.checkstyleFailed, 200, true);
        PowerMockito
                .when(UrlCommunicator.makeGetRequest(Mockito.anyString(),
                                Mockito.anyString()))
                .thenReturn(fakeResult);
    }

    private void initFailedValgrind() throws IOException, TmcCoreException {
        HttpResult fakeResult = new HttpResult(ExampleJson.valgrindFailed, 200, true);
        PowerMockito
                .when(UrlCommunicator.makeGetRequest(Mockito.anyString(),
                                Mockito.anyString()))
                .thenReturn(fakeResult);
    }
}
