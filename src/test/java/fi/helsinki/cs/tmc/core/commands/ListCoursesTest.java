package fi.helsinki.cs.tmc.core.commands;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;

import static org.junit.Assert.assertEquals;

import fi.helsinki.cs.tmc.core.CoreTestSettings;
import fi.helsinki.cs.tmc.core.communication.HttpResult;
import fi.helsinki.cs.tmc.core.communication.TmcApi;
import fi.helsinki.cs.tmc.core.communication.UrlCommunicator;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;
import fi.helsinki.cs.tmc.core.exceptions.TmcServerException;
import fi.helsinki.cs.tmc.core.testhelpers.ExampleJson;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import org.hamcrest.core.IsInstanceOf;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.mockito.Mockito;

import java.io.IOException;
import java.util.List;

public class ListCoursesTest {

    private CoreTestSettings settings = new CoreTestSettings();
    private UrlCommunicator communicator;
    private TmcApi tmcApi;

    @Rule public ExpectedException expectedException = ExpectedException.none();

    @Rule public WireMockRule wireMock = new WireMockRule();

    @Before
    public void setUp() throws IOException {
        communicator = Mockito.mock(UrlCommunicator.class);
        tmcApi = new TmcApi(communicator, settings);

        settings.setUsername("username");
        settings.setPassword("password");

        HttpResult fakeResult = new HttpResult(ExampleJson.allCoursesExample, 200, true);
        Mockito.when(communicator.makeGetRequestWithAuthentication(Mockito.anyString()))
                .thenReturn(fakeResult);
    }

    @Test(expected = TmcCoreException.class)
    public void testNoAuthThrowsException() throws TmcCoreException {
        settings.setUsername(null);
        settings.setPassword(null);
        new ListCourses(settings, tmcApi).call();
    }

    @Test
    public void testWithAuthSuccess() throws Exception {
        List<Course> courses = new ListCourses(settings, tmcApi).call();
        assertEquals("2013_ohpeJaOhja", courses.get(0).getName());
    }

    @Test
    public void listCoursesWillThrowExceptionIfAuthFailedOnServer() throws TmcCoreException {
        expectedException.expectCause(IsInstanceOf.<Throwable>instanceOf(TmcServerException.class));
        wireMock.stubFor(
                get(urlMatching(".*"))
                        .willReturn(WireMock.aResponse().withStatus(401)));

        CoreTestSettings localSettings = new CoreTestSettings();
        localSettings.setUsername("username");
        localSettings.setPassword("password");
        localSettings.setServerAddress("http://localhost:8080");

        new ListCourses(localSettings, new TmcApi(localSettings)).call();
    }
}
