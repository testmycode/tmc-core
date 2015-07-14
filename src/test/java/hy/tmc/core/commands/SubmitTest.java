package hy.tmc.core.commands;

import com.google.common.base.Optional;

import hy.tmc.core.communication.CourseSubmitter;
import hy.tmc.core.communication.SubmissionPoller;
import hy.tmc.core.ClientTmcSettings;
import hy.tmc.core.communication.TmcJsonParser;
import hy.tmc.core.domain.Course;
import hy.tmc.core.domain.submission.SubmissionResult;
import hy.tmc.core.exceptions.ExpiredException;
import hy.tmc.core.exceptions.TmcCoreException;
import hy.tmc.core.testhelpers.ExampleJson;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import net.lingala.zip4j.exception.ZipException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class SubmitTest {

    private Submit submit;
    CourseSubmitter submitterMock;
    private SubmissionPoller interpreter;
    private final String submissionUrl = "/submissions/1781.json?api_version=7";
    ClientTmcSettings settings;

    private void mock() throws ParseException, ExpiredException, IOException, ZipException, TmcCoreException {
        settings = Mockito.mock(ClientTmcSettings.class);
        Mockito.when(settings.getUsername()).thenReturn("Samu");
        Mockito.when(settings.getPassword()).thenReturn("Bossman");
        Mockito.when(settings.getCurrentCourse()).thenReturn(Optional.of(new Course()));
        Mockito
                .when(settings.getFormattedUserData())
                .thenReturn("Bossman:Samu");
        submitterMock = Mockito.mock(CourseSubmitter.class);
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
        submit = new Submit(submitterMock, interpreter, settings);
    }

    /**
     * Check that data checking success.
     */
    @Test
    public void testCheckDataSuccess() throws TmcCoreException, IOException {
        Mockito.when(settings.userDataExists()).thenReturn(true);
        Submit submitCommand = new Submit(settings);
        submitCommand.setParameter("path", "/home/tmccli/testi");
        submitCommand.checkData();
    }

    /**
     * Check that if user didn't give correct data, data checking fails.
     */
    @Test(expected = TmcCoreException.class)
    public void testCheckDataFail() throws TmcCoreException, IOException {
        Submit submitCommand = new Submit(settings);
        submitCommand.checkData();
    }

    @Test(expected = TmcCoreException.class)
    public void checkDataFailIfNoAuth() throws TmcCoreException, IOException {
        Submit submitCommand = new Submit(settings);
        submitCommand.checkData();
    }

    @Test
    public void testSubmissionInterpreter() throws Exception {
        ClientTmcSettings localSettings = setSettings();
        String path = "/home/samutamm/NetBeansProjects/2013_ohpeJaOhja/viikko1";
        Submit submitCommand = new Submit(path, localSettings);
        System.out.println("kurssi: " + localSettings.getCurrentCourse());
        SubmissionResult submission = submitCommand.call();
        System.out.println(submission);
    }

    private ClientTmcSettings setSettings() {
        ClientTmcSettings localSettings = new ClientTmcSettings();
        TmcJsonParser parser = new TmcJsonParser(settings);
        List<Course> courses = parser.getCoursesFromString(ExampleJson.allCoursesExample);
        Course currentCourse = courses.get(5);
        localSettings.setCurrentCourse(currentCourse);
        localSettings.setUsername("test");
        localSettings.setPassword("1234");
        localSettings.setServerAddress("https://tmc.mooc.fi/staging");
        return localSettings;
    }
}
