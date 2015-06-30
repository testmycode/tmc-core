
package hy.tmc.core.commands;

import hy.tmc.core.commands.ListCourses;
import hy.tmc.core.Mailbox;
import hy.tmc.core.communication.HttpResult;
import hy.tmc.core.communication.UrlCommunicator;
import hy.tmc.core.configuration.ClientData;
import hy.tmc.core.exceptions.ProtocolException;
import hy.tmc.core.testhelpers.ExampleJson;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mockito;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertTrue;

@RunWith(PowerMockRunner.class)
@PrepareForTest(UrlCommunicator.class)
public class ListCoursesTest {

    private ListCourses list;

    /**
     * Set up FrontendStub, ListCourses command, power mockito and fake http
     * result.
     */
    @Before
    public void setUp() throws IOException, ProtocolException {
        list = new ListCourses();

        PowerMockito.mockStatic(UrlCommunicator.class);

        HttpResult fakeResult = new HttpResult(ExampleJson.allCoursesExample, 200, true);

        ClientData.setUserData("mockattu", "ei tarvi");
        PowerMockito
                .when(UrlCommunicator.makeGetRequest(
                        Mockito.anyString(), Mockito.anyString()))
                .thenReturn(fakeResult);

    }

    @Test
    public void testCheckDataSuccess() throws ProtocolException {
        ListCourses ls = new ListCourses();
        ClientData.setUserData("asdf", "bsdf");
        ls.checkData();
    }

    @Test(expected = ProtocolException.class)
    public void testNoAuthThrowsException() throws ProtocolException, Exception {
        ClientData.setUserData("", "");
        list.checkData();
        list.call();
    }

    @Test
    public void checkDataTest() throws ProtocolException {
        list.checkData();
    }
}
