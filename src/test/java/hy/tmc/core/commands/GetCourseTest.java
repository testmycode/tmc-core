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
import hy.tmc.core.ClientTmcSettings;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
    private ClientTmcSettings settings;

    @Before
    public void setup() {
        core = new TmcCore();
        settings = Mockito.mock(ClientTmcSettings.class);
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
    public void testCheckDataAddress() throws Exception{
        core.getCourse(createSettingsWith("asda", "asdjh", ""), finalUrl);
    }

    @Test(expected = TmcCoreException.class)
    public void testCheckDataPassword() throws Exception{
        core.getCourse(createSettingsWith("", "asdjh", "adsljads"), finalUrl);
    }
    
    @Test(expected = TmcCoreException.class)
    public void testCheckDataUsername() throws Exception{
        core.getCourse(createSettingsWith("asda", "", "asdasdjkhj"), finalUrl);
    }
    
    @Test
    public void testCheckAllPresent() throws Exception{
        core.getCourse(createSettingsWith("asda", "asdjh", "asdu"), finalUrl);
    }
    
    private ClientTmcSettings createSettingsWith(String password, String username, String address) {
        ClientTmcSettings localSettings = new ClientTmcSettings();
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
        final List<Course> courseResult = new ArrayList<>();
        Futures.addCallback(getCourse, new FutureCallback<Course>() {
            @Override
            public void onSuccess(Course course) {
                courseResult.add(course);
            }

            @Override
            public void onFailure(Throwable thrwbl) {
                System.err.println("virhe: " + thrwbl);
            }
        });

        while (!getCourse.isDone()) {
            Thread.sleep(100);
        }
        assertFalse(courseResult.isEmpty());
        Course course = courseResult.get(0);
        assertEquals(course.getId(), 3);
        assertEquals(course.getName(), "2013_ohpeJaOhja");
    }
}
