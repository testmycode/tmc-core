package fi.helsinki.cs.tmc.core.commands;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertEquals;

import fi.helsinki.cs.tmc.core.CoreTestSettings;
import fi.helsinki.cs.tmc.core.communication.UrlHelper;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;
import fi.helsinki.cs.tmc.core.testhelpers.ExampleJson;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import com.google.common.util.concurrent.ListenableFuture;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class GetCourseTest {

    @Rule public WireMockRule wireMock = new WireMockRule();

    private UrlHelper urlHelper;
    private String finalUrl = "http://127.0.0.1:8080/courses/19.json";
    private String mockUrl;
    private TmcCore core;
    private CoreTestSettings settings;

    public GetCourseTest() {
        settings = new CoreTestSettings();
        settings.setCredentials("test", "1234");
        settings.setCurrentCourse(new Course());
        settings.setServerAddress("https://tmc.mooc.fi/staging");
        settings.setApiVersion("7");
        urlHelper = new UrlHelper(settings);
        mockUrl = urlHelper.withParams("/courses/19.json");
    }

    @Before
    public void setup() {
        core = new TmcCore();
    }

    @Test(expected = TmcCoreException.class)
    public void testCheckDataPassword() throws Exception {
        core.getCourse(createSettingsWith("", "asdjh", "adsljads"), finalUrl);
    }

    @Test(expected = TmcCoreException.class)
    public void testCheckDataUsername() throws Exception {
        core.getCourse(createSettingsWith("asda", "", "asdasdjkhj"), finalUrl);
    }

    @Test
    public void testCheckAllPresent() throws Exception {
        core.getCourse(createSettingsWith("asda", "asdjh", "asdu"), finalUrl);
    }

    private CoreTestSettings createSettingsWith(String password, String username, String address) {
        CoreTestSettings localSettings = new CoreTestSettings();
        localSettings.setPassword(password);
        localSettings.setUsername(username);
        localSettings.setServerAddress(address);
        return localSettings;
    }

    @Test
    public void testCall() throws Exception {
        wireMock.stubFor(
                get(urlEqualTo(mockUrl))
                        .willReturn(
                                WireMock.aResponse()
                                        .withStatus(200)
                                        .withBody(ExampleJson.courseExample)));

        ListenableFuture<Course> getCourse = core.getCourse(settings, finalUrl);
        Course course = getCourse.get();
        assertEquals(course.getId(), 3);
        assertEquals(course.getName(), "2013_ohpeJaOhja");
    }

    @Test
    public void testCallWithCourseName() throws Exception {
        wireMock.stubFor(
                get(urlEqualTo(mockUrl))
                        .willReturn(
                                WireMock.aResponse()
                                        .withStatus(200)
                                        .withBody(ExampleJson.courseExample)));

        ListenableFuture<Course> getCourse = core.getCourseByName(settings, "2013_ohpeJaOhja");
        Course course = getCourse.get();
        assertEquals(course.getId(), 3);
        assertEquals(course.getName(), "2013_ohpeJaOhja");
    }
}
