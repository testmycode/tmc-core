package hy.tmc.core.communication;

import hy.tmc.core.communication.UrlCommunicator;
import hy.tmc.core.communication.HttpResult;
import hy.tmc.core.communication.SubmissionPoller;
import hy.tmc.core.configuration.ClientData;
import hy.tmc.core.exceptions.ProtocolException;
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

    @Before
    public void setup() {
        PowerMockito.mockStatic(UrlCommunicator.class);

        ClientData.setUserData("chang", "paras");

        submissionInterpreter = new SubmissionPoller();
    }

    @After
    public void teardown() {
        ClientData.clearUserData();
    }

    private void initFailedMock() throws IOException, ProtocolException {
        HttpResult fakeResult = new HttpResult(ExampleJson.failedSubmission, 200, true);
        PowerMockito
                .when(UrlCommunicator.makeGetRequest(Mockito.anyString(),
                                Mockito.anyString()))
                .thenReturn(fakeResult);
    }

    private void initSuccessMock() throws IOException, ProtocolException {
        HttpResult fakeResult = new HttpResult(ExampleJson.successfulSubmission, 200, true);
        PowerMockito
                .when(UrlCommunicator.makeGetRequest(Mockito.anyString(),
                                Mockito.anyString()))
                .thenReturn(fakeResult);
    }

    private void initFailedCheckstyle() throws IOException, ProtocolException {
        HttpResult fakeResult = new HttpResult(ExampleJson.checkstyleFailed, 200, true);
        PowerMockito
                .when(UrlCommunicator.makeGetRequest(Mockito.anyString(),
                                Mockito.anyString()))
                .thenReturn(fakeResult);
    }

    private void initFailedValgrind() throws IOException, ProtocolException {
        HttpResult fakeResult = new HttpResult(ExampleJson.valgrindFailed, 200, true);
        PowerMockito
                .when(UrlCommunicator.makeGetRequest(Mockito.anyString(),
                                Mockito.anyString()))
                .thenReturn(fakeResult);
    }
}
