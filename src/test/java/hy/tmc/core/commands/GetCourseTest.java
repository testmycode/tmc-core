package hy.tmc.core.commands;

import com.github.tomakehurst.wiremock.client.WireMock;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.base.Optional;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import hy.tmc.core.testhelpers.ExampleJson;
import hy.tmc.core.TmcCore;
import hy.tmc.core.domain.Course;
import hy.tmc.core.exceptions.TmcCoreException;
import hy.tmc.core.CoreTestSettings;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

public class GetCourseTest {

    @Rule
    public WireMockRule wireMock = new WireMockRule();

    private String finalUrl = "http://127.0.0.1:8080/courses/19.json";
    private String mockUrl = "/courses/19.json?api_version=7";
    private TmcCore core;
    private CoreTestSettings settings;

    @Before
    public void setup() {
        core = new TmcCore();
        settings = Mockito.mock(CoreTestSettings.class);
        Mockito.when(settings.getUsername()).thenReturn("test");
        Mockito.when(settings.getPassword()).thenReturn("1234");
        Mockito.when(settings.getCurrentCourse()).thenReturn(Optional.of(new Course()));
        Mockito
                .when(settings.getFormattedUserData())
                .thenReturn("test:1234");
        Mockito.when(settings.getServerAddress()).thenReturn("https://tmc.mooc.fi/staging");
        Mockito.when(settings.apiVersion()).thenReturn("7");
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
        wireMock.stubFor(get(urlEqualTo(mockUrl))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withBody(ExampleJson.courseExample)));

        ListenableFuture<Course> getCourse = core.getCourse(settings, finalUrl);
        Course course = getCourse.get();
        assertEquals(course.getId(), 3);
        assertEquals(course.getName(), "2013_ohpeJaOhja");
    }

    @Test
    public void testCallWithCourseName() throws Exception {
        wireMock.stubFor(get(urlEqualTo(mockUrl))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withBody(ExampleJson.courseExample)));

        ListenableFuture<Course> getCourse = core.getCourseByName(settings, "2013_ohpeJaOhja");
        Course course = getCourse.get();
        assertEquals(course.getId(), 3);
        assertEquals(course.getName(), "2013_ohpeJaOhja");
    }
}
