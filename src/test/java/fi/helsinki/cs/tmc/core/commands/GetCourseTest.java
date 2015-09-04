package fi.helsinki.cs.tmc.core.commands;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import static org.junit.Assert.assertEquals;

import fi.helsinki.cs.tmc.core.CoreTestSettings;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.communication.UrlHelper;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;
import fi.helsinki.cs.tmc.core.testhelpers.ExampleJson;

import com.github.tomakehurst.wiremock.client.UrlMatchingStrategy;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import com.google.common.util.concurrent.ListenableFuture;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

public class GetCourseTest {

    /*
    *  Adding 0 to constructor makes this work on all ports, but the testCallWithCourseName() -test brakes
    *  for some reason. The reason lies somewhere deep inside this tmc-core. I think GetCourse - class's pollServerForCourseUrl()
    *  is the reason. It gives address directly from the .json. Then when .get() of the getCourse - object is called it
    *  makes the request to :8080 port instead of the wiremocks random open port.
    * */
    @Rule public WireMockRule wireMock = new WireMockRule(0);
    private String serverAddress = "http://127.0.0.1:";

    private UrlHelper urlHelper;
    private URI finalUrl;
    private UrlMatchingStrategy mockUrl;
    private TmcCore core;
    private CoreTestSettings settings;

    public GetCourseTest() throws URISyntaxException {
        settings = new CoreTestSettings();
        settings.setCredentials("test", "1234");
        settings.setCurrentCourse(new Course());
        settings.setApiVersion("7");
        urlHelper = new UrlHelper(settings);
        mockUrl = urlPathEqualTo("/courses/19.json");
    }

    @Before
    public void setup() throws URISyntaxException {
        serverAddress += wireMock.port();
        settings.setServerAddress(serverAddress);
        finalUrl = new URI(serverAddress + "/courses/19.json");
        core = new TmcCore(settings);
    }

    @Test(expected = TmcCoreException.class)
    public void testCheckDataPassword() throws Exception {
        core = new TmcCore(createSettingsWith("", "asdjh", "adsljads"));
        core.getCourse(finalUrl);
    }

    @Test(expected = TmcCoreException.class)
    public void testCheckDataUsername() throws Exception {
        core = new TmcCore(createSettingsWith("asda", "", "asdasdjkhj"));
        core.getCourse(finalUrl);
    }

    @Test
    public void testCheckAllPresent() throws Exception {
        core = new TmcCore(createSettingsWith("asda", "asdjh", "asdu"));
        core.getCourse(finalUrl);
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
                get(mockUrl)
                        .willReturn(
                                aResponse().withStatus(200).withBody(ExampleJson.courseExample)));

        ListenableFuture<Course> getCourse = core.getCourse(finalUrl);
        Course course = getCourse.get();
        assertEquals(course.getId(), 3);
        assertEquals(course.getName(), "2013_ohpeJaOhja");
    }

    @Test
    public void testCallWithCourseName() throws Exception {

        wireMock.stubFor(
                get(urlPathEqualTo("/courses.json"))
                        .willReturn(aResponse().withBody(ExampleJson.allCoursesExample)));
        wireMock.stubFor(
                get(urlPathEqualTo("/courses/3.json"))
                        .willReturn(
                                aResponse().withStatus(200).withBody(ExampleJson.courseExample)));


        ListenableFuture<Course> getCourse = core.getCourseByName("2013_ohpeJaOhja");
        Course course = getCourse.get();
        assertEquals(course.getId(), 3);
        assertEquals(course.getName(), "2013_ohpeJaOhja");
    }
}
