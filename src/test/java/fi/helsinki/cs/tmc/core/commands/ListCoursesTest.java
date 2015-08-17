package fi.helsinki.cs.tmc.core.commands;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import static org.junit.Assert.assertEquals;

import fi.helsinki.cs.tmc.core.CoreTestSettings;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.communication.HttpResult;
import fi.helsinki.cs.tmc.core.communication.UrlCommunicator;
import fi.helsinki.cs.tmc.core.communication.UrlHelper;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;
import fi.helsinki.cs.tmc.core.exceptions.TmcServerException;
import fi.helsinki.cs.tmc.core.testhelpers.ExampleJson;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.util.concurrent.ListenableFuture;

import org.hamcrest.core.IsInstanceOf;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.mockito.Mockito;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ListCoursesTest {

    private ListCourses list;
    CoreTestSettings settings = new CoreTestSettings();
    UrlCommunicator communicator;

    @Rule public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws IOException, TmcCoreException {
        communicator = Mockito.mock(UrlCommunicator.class);

        settings.setUsername("mockattu");
        settings.setPassword("ei tarvi");
        HttpResult fakeResult = new HttpResult(ExampleJson.allCoursesExample, 200, true);
        Mockito.when(communicator.makeGetRequestWithAuthentication(Mockito.anyString()))
                .thenReturn(fakeResult);

        list = new ListCourses(settings, communicator);
    }

    @Test
    public void testCheckDataSuccess() throws TmcCoreException, IOException {
        ListCourses ls = new ListCourses(settings, communicator);
        settings.setUsername("asdf");
        settings.setPassword("bsdf");
        ls.call();
    }

    @Test(expected = TmcCoreException.class)
    public void testNoAuthThrowsException() throws TmcCoreException, Exception {
        settings.setUsername("");
        settings.setPassword("");
        list.call();
    }

    @Test
    public void testWithAuthSuccess() throws Exception {
        List<Course> courses = list.call();
        assertEquals("2013_ohpeJaOhja", courses.get(0).getName());
    }

    @Rule public WireMockRule wireMock = new WireMockRule();

    @Test
    public void listCoursesWillThrowExceptionIfAuthFailedOnServer()
            throws ExecutionException, InterruptedException, TmcCoreException, URISyntaxException {
        expectedException.expectCause(IsInstanceOf.<Throwable>instanceOf(TmcServerException.class));
        wireMock.stubFor(
                get(WireMock.urlPathEqualTo("/courses.json"))
                        .willReturn(WireMock.aResponse().withStatus(401)));

        CoreTestSettings localSettings = new CoreTestSettings();
        localSettings.setUsername("testy");
        localSettings.setPassword("1234");
        localSettings.setServerAddress("http://localhost:8080");
        TmcCore core = new TmcCore(localSettings);
        ListenableFuture<List<Course>> courses = core.listCourses();
        courses.get();
    }
}
