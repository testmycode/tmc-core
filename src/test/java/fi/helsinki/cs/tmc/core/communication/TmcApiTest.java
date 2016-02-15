package fi.helsinki.cs.tmc.core.communication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;

import static org.powermock.api.mockito.PowerMockito.mock;

import fi.helsinki.cs.tmc.core.CoreTestSettings;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;
import fi.helsinki.cs.tmc.core.testhelpers.ExampleJson;

import com.google.common.base.Optional;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mockito;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@RunWith(PowerMockRunner.class)
@PrepareForTest(UrlCommunicator.class)
public class TmcApiTest {

    private CoreTestSettings settings;
    private UrlCommunicator urlCommunicator;
    private TmcApi tmcApi;
    /**
     * Mocks UrlCommunicator.
     */
    @Before
    public void setup() throws IOException, TmcCoreException {
        urlCommunicator = mock(UrlCommunicator.class);
        settings = new CoreTestSettings();
        settings.setUsername("chang");
        settings.setPassword("rajani");
        tmcApi = new TmcApi(urlCommunicator, settings);
        PowerMockito.mockStatic(UrlCommunicator.class);

        HttpResult fakeResult = new HttpResult(ExampleJson.allCoursesExample, 200, true);

        Mockito.when(urlCommunicator.makeGetRequest(any(URI.class), anyString()))
                .thenReturn(fakeResult);
        Mockito.when(urlCommunicator.makeGetRequestWithAuthentication(any(URI.class)))
                .thenReturn(fakeResult);
    }

    private void mockSubmissionUrl() throws IOException {
        HttpResult fakeResult = new HttpResult(ExampleJson.successfulSubmission, 200, true);
        Mockito.when(urlCommunicator.makeGetRequest(any(URI.class), anyString()))
                .thenReturn(fakeResult);
        Mockito.when(urlCommunicator.makeGetRequestWithAuthentication(any(URI.class)))
                .thenReturn(fakeResult);
    }

    @Test
    public void parsesSubmissionUrlFromJson() throws IOException {
        HttpResult fakeResult = new HttpResult(ExampleJson.submitResponse, 200, true);
        Mockito.when(urlCommunicator.makeGetRequestWithAuthentication(any(URI.class)))
                .thenReturn(fakeResult);
        JsonObject json = new JsonParser().parse(fakeResult.getData()).getAsJsonObject();
        URI submissionUrl = URI.create(json.get("submission_url").getAsString());
        assertEquals(
                URI.create("https://example.com/staging/submissions/1781.json?api_version=7"),
                submissionUrl);
    }

    @Test
    public void parsesPasteUrlFromJson() throws IOException, TmcCoreException {
        HttpResult fakeResult = new HttpResult(ExampleJson.pasteResponse, 200, true);
        Mockito.when(
                        urlCommunicator.makeGetRequest(
                                URI.create(Mockito.anyString()), Mockito.anyString()))
                .thenReturn(fakeResult);

        JsonObject json = new JsonParser().parse(fakeResult.getData()).getAsJsonObject();
        URI pasteUrl = URI.create(json.get("paste_url").getAsString());

        assertEquals(
                URI.create("https://example.com/staging/paste/ynpw7_mZZGk3a9PPrMWOOQ"),
                pasteUrl);
    }

    String realAddress = "http://real.address.fi";

    private void mockCourse(String url) throws IOException {
        HttpResult fakeResult = new HttpResult(ExampleJson.courseExample, 200, true);
        Mockito.when(urlCommunicator.makeGetRequestWithAuthentication(eq(URI.create(url))))
                .thenReturn(fakeResult);
    }

    @Test
    public void canFetchOneCourse() throws IOException, TmcCoreException, URISyntaxException {
        HttpResult fakeResult = new HttpResult(ExampleJson.courseExample, 200, true);
        Mockito.when(
                        urlCommunicator.makeGetRequestWithAuthentication(
                                argThat(new UriContains("/courses/3"))))
                .thenReturn(fakeResult);

        Optional<Course> course = tmcApi.getCourse(3);
        assertTrue(course.isPresent());
        assertEquals("test-course", course.get().getName());
    }
}
