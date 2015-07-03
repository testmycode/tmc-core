package hy.tmc.core.communication;

import com.google.common.base.Optional;
import static org.junit.Assert.assertTrue;

import hy.tmc.core.configuration.ClientTmcSettings;
import hy.tmc.core.domain.Course;
import hy.tmc.core.domain.submission.SubmissionResult;
import hy.tmc.core.exceptions.TmcCoreException;
import hy.tmc.core.testhelpers.ExampleJson;
import java.io.IOException;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Matchers.contains;
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
    public void setup() throws IOException, TmcCoreException {
        PowerMockito.mockStatic(UrlCommunicator.class);
        HttpResult fakeResult = new HttpResult(ExampleJson.allCoursesExample, 200, true);
        ClientTmcSettings.setUserData("chang", "paras");
        PowerMockito
                .when(UrlCommunicator.makeGetRequest(Mockito.anyString(),
                                Mockito.anyString()))
                .thenReturn(fakeResult);

    }
        
    @Test
    public void getsExercisesCorrectlyFromCourseJson() throws IOException, TmcCoreException {
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
    public void getsLastExerciseOfCourseJson() throws IOException, TmcCoreException {
        HttpResult fakeResult = new HttpResult(ExampleJson.courseExample, 200, true);
        PowerMockito
                .when(UrlCommunicator.makeGetRequest(Mockito.eq("ankka"),
                                Mockito.anyString()))
                .thenReturn(fakeResult);
        String names = TmcJsonParser.getExerciseNames("ankka");

        assertTrue(names.contains("viikko11-Viikko11_147.Laskin"));
    }

    @Test
    public void parsesSubmissionUrlFromJson() throws IOException, TmcCoreException {
        HttpResult fakeResult = new HttpResult(ExampleJson.submitResponse, 200, true);
        PowerMockito
                .when(UrlCommunicator.makeGetRequest(Mockito.anyString(),
                                Mockito.anyString()))
                .thenReturn(fakeResult);
        assertEquals("http://127.0.0.1:8080/submissions/1781.json?api_version=7", TmcJsonParser.getSubmissionUrl(fakeResult));
    }

    @Test
    public void parsesPasteUrlFromJson() throws IOException, TmcCoreException {
        HttpResult fakeResult = new HttpResult(ExampleJson.pasteResponse, 200, true);
        PowerMockito
                .when(UrlCommunicator.makeGetRequest(Mockito.anyString(),
                                Mockito.anyString()))
                .thenReturn(fakeResult);
        assertEquals("https://tmc.mooc.fi/staging/paste/ynpw7_mZZGk3a9PPrMWOOQ", TmcJsonParser.getPasteUrl(fakeResult));
    }
    
    String realAddress = "http://real.address.fi";

    

    @After
    public void teardown() {
        ClientTmcSettings.clearUserData();
    }

    private void mockCourse(String url) throws IOException, TmcCoreException {
        HttpResult fakeResult = new HttpResult(ExampleJson.courseExample, 200, true);
        PowerMockito
                .when(UrlCommunicator.makeGetRequest(Mockito.eq(url),
                                Mockito.anyString()))
                .thenReturn(fakeResult);
    }

    @Test
    public void getsExercisesCorrectlyFromCourseJSON() throws IOException, TmcCoreException {
        mockCourse(realAddress);
        String names = TmcJsonParser.getExerciseNames(realAddress);

        assertTrue(names.contains("viikko1-Viikko1_001.Nimi"));
        assertTrue(names.contains("viikko1-Viikko1_002.HeiMaailma"));
        assertTrue(names.contains("viikko1-Viikko1_003.Kuusi"));
    }

    @Test
    public void getsLastExerciseOfCourseJSON() throws IOException, TmcCoreException {
        mockCourse(realAddress);
        String names = TmcJsonParser.getExerciseNames(realAddress);

        assertTrue(names.contains("viikko11-Viikko11_147.Laskin"));
    }

    @Test
    public void canFetchOneCourse() throws IOException, TmcCoreException {
        HttpResult fakeResult = new HttpResult(ExampleJson.courseExample, 200, true);
        PowerMockito
                .when(UrlCommunicator.makeGetRequest(contains("/courses/3"),
                                Mockito.anyString()))
                .thenReturn(fakeResult);

        Optional<Course> course = TmcJsonParser.getCourse(3);
        assertTrue(course.isPresent());
        assertEquals("2013_ohpeJaOhja", course.get().getName());

    }

    private void mockSubmissionUrl() throws IOException, TmcCoreException {
        PowerMockito.mockStatic(UrlCommunicator.class);

        HttpResult fakeResult = new HttpResult(ExampleJson.successfulSubmission, 200, true);
        PowerMockito
                .when(UrlCommunicator.makeGetRequest(Mockito.anyString(),
                                Mockito.anyString()))
                .thenReturn(fakeResult);
    }

    @Test
    public void canFetchSubmissionData() throws IOException, TmcCoreException {
        mockSubmissionUrl();
        SubmissionResult result = TmcJsonParser.getSubmissionResult("http://real.address.fi");
        assertNotNull(result);
        assertEquals("2014-mooc-no-deadline", result.getCourse());
    }
}
