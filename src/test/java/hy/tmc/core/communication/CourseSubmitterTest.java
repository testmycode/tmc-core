package hy.tmc.core.communication;

import hy.tmc.core.communication.UrlCommunicator;
import hy.tmc.core.communication.HttpResult;
import hy.tmc.core.communication.CourseSubmitter;
import com.google.common.base.Optional;
import static org.junit.Assert.assertEquals;

import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import hy.tmc.core.configuration.ClientData;
import hy.tmc.core.configuration.ConfigHandler;
import hy.tmc.core.domain.Course;
import hy.tmc.core.exceptions.ExpiredException;
import hy.tmc.core.exceptions.ProtocolException;
import hy.tmc.core.testhelpers.ExampleJson;
import hy.tmc.core.testhelpers.ProjectRootFinderStub;
import hy.tmc.core.testhelpers.ZipperStub;
import hy.tmc.core.zipping.DefaultRootDetector;
import hy.tmc.core.zipping.ProjectRootFinder;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Map;
import net.lingala.zip4j.exception.ZipException;
import org.apache.http.entity.mime.content.FileBody;
import org.junit.After;
import static org.junit.Assert.assertFalse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PowerMockRunner.class)
@PrepareForTest(UrlCommunicator.class)
public class CourseSubmitterTest {

    private CourseSubmitter courseSubmitter;
    private ProjectRootFinderStub rootFinder;
    private ProjectRootFinder realFinder;

    /**
     * Mocks components that use Internet.
     */
    @Before
    public void setup() throws IOException, ProtocolException {
        new ConfigHandler().writeServerAddress("http://mooc.fi/staging");
        PowerMockito.mockStatic(UrlCommunicator.class);
        rootFinder = new ProjectRootFinderStub();
        this.courseSubmitter = new CourseSubmitter(rootFinder, new ZipperStub());
        ClientData.setUserData("chang", "rajani");

        mockUrlCommunicator("/courses.json?api_version=7", ExampleJson.allCoursesExample);
        mockUrlCommunicator("courses/3.json?api_version=7", ExampleJson.courseExample);
        mockUrlCommunicator("courses/19.json?api_version=7", ExampleJson.noDeadlineCourseExample);
        mockUrlCommunicator("courses/21.json?api_version=7", ExampleJson.expiredCourseExample);
        mockUrlCommunicatorWithFile("https://tmc.mooc.fi/staging/exercises/285/submissions.json?api_version=7", ExampleJson.submitResponse);
        mockUrlCommunicatorWithFile("https://tmc.mooc.fi/staging/exercises/287/submissions.json?api_version=7", ExampleJson.pasteResponse);
        realFinder = new ProjectRootFinder(new DefaultRootDetector());
        mockUrlCommunicatorWithFile("https://tmc.mooc.fi/staging/exercises/1228/submissions.json?api_version=7", ExampleJson.submitResponse);
        mockUrlCommunicatorWithFile("https://tmc.mooc.fi/staging/exercises/1228/submissions.json?api_version=7", ExampleJson.pasteResponse);
    }

    @After
    public void clear() throws IOException {
        ClientData.clearUserData();
        new ConfigHandler().writeServerAddress("");
    }

    @Test
    public void testGetExerciseName() {
        final String path = "/home/test/ohpe-test/viikko_01";
        rootFinder.setReturnValue(path);
        String[] names = courseSubmitter.getExerciseName(path);
        assertEquals("viikko_01", names[names.length - 1]);
    }

    @Test
    public void testFindCourseByCorrectPath() throws IOException, ProtocolException {
        final String path = "/home/kansio/toinen/c-demo/viikko_01";
        Optional<Course> course = realFinder.findCourseByPath(path.split(File.separator));
        assertEquals(7, course.get().getId());
        final String path2 = "/home/kansio/toinen/OLEMATON/viikko_01";
        Optional<Course> course2 = realFinder.findCourseByPath(path2.split(File.separator));
        assertFalse(course2.isPresent());
    }

    @Test
    public void testSubmitWithOneParam() throws IOException, ParseException, ExpiredException, IllegalArgumentException, ZipException, ProtocolException {
        String testPath = "/home/test/2014-mooc-no-deadline/viikko1/viikko1-Viikko1_001.Nimi";
        rootFinder.setReturnValue(testPath);
        String submissionPath = "http://127.0.0.1:8080/submissions/1781.json?api_version=7";
        String result = courseSubmitter.submit(testPath);
        assertEquals(submissionPath, result);
    }
    
    @Test(expected = ExpiredException.class)
    public void testSubmitWithExpiredExercise() throws IOException, ParseException, ExpiredException, IllegalArgumentException, ZipException, ProtocolException {
        String testPath = "/home/test/k2015-tira/viikko01/tira1.1";

        rootFinder.setReturnValue(testPath);
        String submissionPath = "http://127.0.0.1:8080/submissions/1781.json?api_version=7";
        String result = courseSubmitter.submit(testPath);
    }
    
    @Test
    public void submitWithPasteReturnsPasteUrl() throws IOException, ParseException, ExpiredException, IllegalArgumentException, ZipException, ProtocolException {
        String testPath = "/home/test/2014-mooc-no-deadline/viikko1/viikko1-Viikko1_001.Nimi";
        rootFinder.setReturnValue(testPath);
        String pastePath = "https://tmc.mooc.fi/staging/paste/ynpw7_mZZGk3a9PPrMWOOQ";
        String result = courseSubmitter.submitPaste(testPath);
        assertEquals(pastePath, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void submitWithPasteFromBadPathThrowsException() throws IOException, ParseException, ExpiredException, IllegalArgumentException, ZipException, ProtocolException {
        String testPath = "/home/test/2014-mooc-no-deadline/viikko1/feikeintehtava";
        rootFinder.setReturnValue(testPath);
        String result = courseSubmitter.submit(testPath);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubmitWithNonexistentExercise() throws IOException, ParseException, ExpiredException, IllegalArgumentException, ZipException, ProtocolException {
        String testPath = "/home/test/2014-mooc-no-deadline/viikko1/feikkitehtava";
        rootFinder.setReturnValue(testPath);
        String result = courseSubmitter.submit(testPath);
    }

    @Test(expected = IllegalArgumentException.class)
    public void submitWithNonExistentCourseThrowsException() throws IOException, ParseException, ExpiredException, IllegalArgumentException, ZipException, ProtocolException {
        String testPath = "/home/test/2013_FEIKKIKURSSI/viikko_01/viikko1-Viikko1_001.Nimi";
        rootFinder.setReturnValue(testPath);
        String result = courseSubmitter.submit(testPath);
    }

     private void mockUrlCommunicator(String pieceOfUrl, String returnValue) throws IOException, ProtocolException {
        HttpResult fakeResult = new HttpResult(returnValue, 200, true);
        PowerMockito
                .when(UrlCommunicator.makeGetRequest(Mockito.contains(pieceOfUrl),
                                Mockito.anyString()))
                .thenReturn(fakeResult);
    }

    @SuppressWarnings("unchecked")
    private void mockUrlCommunicatorWithFile(String url, String returnValue) throws IOException, ProtocolException {
        HttpResult fakeResult = new HttpResult(returnValue, 200, true);
        PowerMockito
                .when(UrlCommunicator.makePostWithFile(Mockito.any(FileBody.class),
                                Mockito.contains(url), Mockito.any(Map.class)))
                .thenReturn(fakeResult);
    }
}
