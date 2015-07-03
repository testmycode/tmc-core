
package hy.tmc.core.commands;

import hy.tmc.core.communication.HttpResult;
import hy.tmc.core.communication.UrlCommunicator;
import hy.tmc.core.testhelpers.ClientTmcSettings;
import hy.tmc.core.domain.Course;
import hy.tmc.core.exceptions.TmcCoreException;
import hy.tmc.core.testhelpers.ExampleJson;

import java.io.IOException;
import java.util.List;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mockito;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;


@RunWith(PowerMockRunner.class)
@PrepareForTest(UrlCommunicator.class)
public class ListCoursesTest {

    private ListCourses list;

    /**
     * Set up FrontendStub, ListCourses command, power mockito and fake http
     * result.
     */
    @Before
    public void setUp() throws IOException, TmcCoreException {
        list = new ListCourses();

        PowerMockito.mockStatic(UrlCommunicator.class);

        HttpResult fakeResult = new HttpResult(ExampleJson.allCoursesExample, 200, true);

        ClientTmcSettings.setUserData("mockattu", "ei tarvi");
        PowerMockito
                .when(UrlCommunicator.makeGetRequest(
                        Mockito.anyString(), Mockito.anyString()))
                .thenReturn(fakeResult);

    }

    @Test
    public void testCheckDataSuccess() throws TmcCoreException {
        ListCourses ls = new ListCourses();
        ClientTmcSettings.setUserData("asdf", "bsdf");
        ls.checkData();
    }

    @Test(expected = TmcCoreException.class)
    public void testNoAuthThrowsException() throws TmcCoreException, Exception {
        ClientTmcSettings.setUserData("", "");
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
}
