package hy.tmc.core.commands;

import com.github.tomakehurst.wiremock.client.WireMock;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import hy.tmc.core.TmcCore;
import hy.tmc.core.communication.HttpResult;
import hy.tmc.core.communication.UrlCommunicator;
import hy.tmc.core.CoreTestSettings;
import hy.tmc.core.domain.Course;
import hy.tmc.core.exceptions.TmcCoreException;
import hy.tmc.core.exceptions.TmcServerException;
import hy.tmc.core.testhelpers.ExampleJson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

public class ListCoursesTest {

    private ListCourses list;
    CoreTestSettings settings = new CoreTestSettings();
    UrlCommunicator communicator;

    /**
     * Set up FrontendStub, ListCourses command, power mockito and fake http result.
     */
    @Before
    public void setUp() throws IOException, TmcCoreException {
        communicator = Mockito.mock(UrlCommunicator.class);

        settings.setUsername("mockattu");
        settings.setPassword("ei tarvi");
        HttpResult fakeResult = new HttpResult(ExampleJson.allCoursesExample, 200, true);
        Mockito
                .when(communicator.makeGetRequestWithAuthentication(
                                Mockito.anyString()))
                .thenReturn(fakeResult);

        list = new ListCourses(settings, communicator);

    }

    @Test
    public void testCheckDataSuccess() throws TmcCoreException {
        ListCourses ls = new ListCourses(settings, communicator);
        settings.setUsername("asdf");
        settings.setPassword("bsdf");
        ls.checkData();
    }

    @Test(expected = TmcCoreException.class)
    public void testNoAuthThrowsException() throws TmcCoreException, Exception {
        settings.setUsername("");
        settings.setPassword("");
        list.checkData();
        list.call();
    }

    @Test
    public void checkDataTest() throws TmcCoreException {
        list.checkData();
    }

    @Test
    public void testWithAuthSuccess() throws Exception {
        List<Course> courses = list.call();
        assertEquals("2013_ohpeJaOhja", courses.get(0).getName());
    }

    @Rule
    public WireMockRule wireMock = new WireMockRule();
    
    @Test
    public void ListCoursesWillThrowExceptionIfAuthFailedOnServer() throws Exception {
        wireMock.stubFor(get(urlEqualTo("/courses.json?api_version=7"))
                .willReturn(WireMock.aResponse()
                        .withStatus(401)));
        
        CoreTestSettings localSettings = new CoreTestSettings();
        localSettings.setUsername("testy");
        localSettings.setPassword("1234");
        localSettings.setServerAddress("http://localhost:8080");
        TmcCore core = new TmcCore();
        final List<Throwable> exception = new ArrayList<>();
        ListenableFuture<List<Course>> courses = core.listCourses(localSettings);
        Futures.addCallback(courses, new FutureCallback<List<Course>>() {

            @Override
            public void onSuccess(List<Course> courses) {
            }

            @Override
            public void onFailure(Throwable thrwbl) {
                exception.add(thrwbl);
            }
        });
        while (!courses.isDone()) {
            Thread.sleep(100);
        }
        assertFalse(exception.isEmpty());
        assertEquals(exception.get(0).getClass(), TmcServerException.class);
    }
}
