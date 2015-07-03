package hy.tmc.core.commands;


import com.google.common.base.Optional;

import hy.tmc.core.communication.CourseSubmitter;
import hy.tmc.core.communication.SubmissionPoller;
import hy.tmc.core.configuration.ClientTmcSettings;
import hy.tmc.core.domain.Course;
import hy.tmc.core.exceptions.ExpiredException;
import hy.tmc.core.exceptions.TmcCoreException;

import java.io.IOException;
import java.text.ParseException;

import net.lingala.zip4j.exception.ZipException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ClientTmcSettings.class)
public class SubmitTest {

    private Submit submit;
    CourseSubmitter submitterMock;
    private SubmissionPoller interpreter;
    private final String submissionUrl = "/submissions/1781.json?api_version=7";

    private void mock() throws ParseException, ExpiredException, IOException, ZipException, TmcCoreException {
        submitterMock = Mockito.mock(CourseSubmitter.class);
        PowerMockito.mockStatic(ClientTmcSettings.class);
        PowerMockito
                .when(ClientTmcSettings.getCurrentCourse(anyString()))
                .thenReturn(Optional.<Course>of(new Course()));
        PowerMockito
                .when(ClientTmcSettings.getFormattedUserData())
                .thenReturn("Bossman:Samu");
        PowerMockito
                .when(ClientTmcSettings.userDataExists())
                .thenReturn(true);

        when(submitterMock.submit(anyString())).thenReturn("http://127.0.0.1:8080/submissions/1781.json?api_version=7");

        interpreter = Mockito.mock(SubmissionPoller.class);
    }

    @Before
    public void setup() throws
            IOException, InterruptedException, IOException, ParseException, ExpiredException, Exception {
        mock();
        submitterMock = Mockito.mock(CourseSubmitter.class);
        when(submitterMock.submit(anyString())).thenReturn("http://127.0.0.1:8080" + submissionUrl);
        interpreter = Mockito.mock(SubmissionPoller.class);
        submit = new Submit(submitterMock, interpreter);
        ClientTmcSettings.setUserData("Bossman", "Samu");
    }

    @After
    public void clean() {
        ClientTmcSettings.clearUserData();
    }

   
    /**
     * Check that data checking success.
     */
    @Test
    public void testCheckDataSuccess() throws TmcCoreException, IOException {
        Submit submitCommand = new Submit();
        submitCommand.setParameter("path", "/home/tmccli/testi");
        submitCommand.checkData();
    }

    /**
     * Check that if user didn't give correct data, data checking fails.
     */
    @Test(expected = TmcCoreException.class)
    public void testCheckDataFail() throws TmcCoreException, IOException {
        Submit submitCommand = new Submit();
        submitCommand.checkData();
    }

    @Test(expected = TmcCoreException.class)
    public void checkDataFailIfNoAuth() throws TmcCoreException, IOException {
        Submit submitCommand = new Submit();
        ClientTmcSettings.clearUserData();
        submitCommand.checkData();
    }

}
