package hy.tmc.core.communication;

import hy.tmc.core.communication.UrlCommunicator;
import hy.tmc.core.communication.HttpResult;
import hy.tmc.core.communication.TmcJsonParser;
import static org.junit.Assert.assertTrue;

import hy.tmc.core.configuration.ClientData;
import hy.tmc.core.exceptions.ProtocolException;
import hy.tmc.core.synchronization.TmcServiceScheduler;
import hy.tmc.core.testhelpers.ExampleJson;
import java.io.IOException;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(UrlCommunicator.class)
public class TmcJsonParserTest {

    /**
     * Mocks UrlCommunicator.
     */
    @Before
    public void setup() throws IOException, ProtocolException {
        TmcServiceScheduler.getScheduler().stop();
        PowerMockito.mockStatic(UrlCommunicator.class);
        HttpResult fakeResult = new HttpResult(ExampleJson.allCoursesExample, 200, true);
        ClientData.setUserData("chang", "paras");
        PowerMockito
                .when(UrlCommunicator.makeGetRequest(Mockito.anyString(),
                                Mockito.anyString()))
                .thenReturn(fakeResult);

    }
    
    @Test
    public void getsExercisesCorrectlyFromCourseJson() throws IOException, ProtocolException {
        HttpResult fakeResult = new HttpResult(ExampleJson.courseExample, 200, true);
        PowerMockito
                .when(UrlCommunicator.makeGetRequest(Mockito.eq("ankka"),
                                Mockito.anyString()))
                .thenReturn(fakeResult);
        String names = TmcJsonParser.getExerciseNames("ankka");

        assertTrue(names.contains("viikko1-Viikko1_001.Nimi"));
        assertTrue(names.contains("viikko1-Viikko1_002.HeiMaailma"));
        assertTrue(names.contains("viikko1-Viikko1_003.Kuusi"));
    }

    @Test
    public void getsLastExerciseOfCourseJson() throws IOException, ProtocolException {
        HttpResult fakeResult = new HttpResult(ExampleJson.courseExample, 200, true);
        PowerMockito
                .when(UrlCommunicator.makeGetRequest(Mockito.eq("ankka"),
                                Mockito.anyString()))
                .thenReturn(fakeResult);
        String names = TmcJsonParser.getExerciseNames("ankka");

        assertTrue(names.contains("viikko11-Viikko11_147.Laskin"));
    }

    @Test
    public void canFetchOneCourse() {
        // TODO: implement!
    }

    @Test
    public void parsesSubmissionUrlFromJson() throws IOException, ProtocolException {
        HttpResult fakeResult = new HttpResult(ExampleJson.submitResponse, 200, true);
        PowerMockito
                .when(UrlCommunicator.makeGetRequest(Mockito.anyString(),
                                Mockito.anyString()))
                .thenReturn(fakeResult);
        assertEquals("http://127.0.0.1:8080/submissions/1781.json?api_version=7", TmcJsonParser.getSubmissionUrl(fakeResult));
    }

    @Test
    public void parsesPasteUrlFromJson() throws IOException, ProtocolException {
        HttpResult fakeResult = new HttpResult(ExampleJson.pasteResponse, 200, true);
        PowerMockito
                .when(UrlCommunicator.makeGetRequest(Mockito.anyString(),
                                Mockito.anyString()))
                .thenReturn(fakeResult);
        assertEquals("https://tmc.mooc.fi/staging/paste/ynpw7_mZZGk3a9PPrMWOOQ", TmcJsonParser.getPasteUrl(fakeResult));
    }
}
