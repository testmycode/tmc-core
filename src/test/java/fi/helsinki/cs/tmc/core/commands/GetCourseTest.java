package fi.helsinki.cs.tmc.core.commands;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import static org.junit.Assert.assertEquals;

import fi.helsinki.cs.tmc.core.CoreTestSettings;
import fi.helsinki.cs.tmc.core.TmcCore;
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

    @Rule public WireMockRule wireMock = new WireMockRule(0);
    private String serverAddress = "http://127.0.0.1:";

    private URI finalUrl;
    private UrlMatchingStrategy mockUrl;

    private TmcCore core;
    private CoreTestSettings settings;

    public GetCourseTest() throws URISyntaxException {
        settings = new CoreTestSettings();
        settings.setCredentials("test", "1234");
        settings.setCurrentCourse(new Course());
        settings.setApiVersion("7");
        mockUrl = urlPathEqualTo("/courses/19.json");
    }

    private void performWiremockStubbing() {
        wireMock.stubFor(
                get(urlPathEqualTo("/courses.json"))
                        .willReturn(aResponse().withBody(ExampleJson
                                .allCoursesExample.replaceAll("http://example.com", serverAddress))));
        wireMock.stubFor(
                get(urlPathEqualTo("/courses/3.json"))
                        .willReturn(
                                aResponse().withStatus(200).withBody(ExampleJson.courseExample)));
        wireMock.stubFor(
                get(mockUrl)
                        .willReturn(
                                aResponse().withStatus(200).withBody(ExampleJson.courseExample)));
    }

    @Before
    public void setup() throws URISyntaxException {
        serverAddress += wireMock.port();
        settings.setServerAddress(serverAddress);
        finalUrl = new URI(serverAddress + "/courses/19.json");
        core = new TmcCore(settings);
        performWiremockStubbing();
    }

    private CoreTestSettings createSettingsWith(String password, String username, String address) {
        CoreTestSettings localSettings = new CoreTestSettings();
        localSettings.setPassword(password);
        localSettings.setUsername(username);
        localSettings.setServerAddress(address);
        return localSettings;
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

    @Test
    public void testCall() throws Exception {

        ListenableFuture<Course> getCourse = core.getCourse(finalUrl);
        Course course = getCourse.get();
        assertEquals(course.getId(), 3);
        assertEquals(course.getName(), "2013_ohpeJaOhja");
    }

    @Test
    public void testCallWithCourseName() throws Exception {

        ListenableFuture<Course> getCourse = core.getCourseByName("2013_ohpeJaOhja");
        Course course = getCourse.get();
        assertEquals(course.getId(), 3);
        assertEquals(course.getName(), "2013_ohpeJaOhja");
    }
}
