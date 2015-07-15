package hy.tmc.core.communication;

import com.google.common.base.Optional;
import edu.emory.mathcs.backport.java.util.Arrays;
import static org.junit.Assert.assertEquals;

import org.mockito.Mockito;

import hy.tmc.core.testhelpers.ClientTmcSettings;
import hy.tmc.core.domain.Course;
import hy.tmc.core.exceptions.ExpiredException;
import hy.tmc.core.exceptions.TmcCoreException;
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
import static org.junit.Assert.assertFalse;
import org.junit.Before;
import org.junit.Test;
import static org.powermock.api.mockito.PowerMockito.mock;


public class CourseSubmitterTest {

    private CourseSubmitter courseSubmitter;
    private UrlCommunicator urlCommunicator;
    private TmcJsonParser jsonParser;
    private ProjectRootFinderStub rootFinder;
    private ProjectRootFinder realFinder;
    private ClientTmcSettings settings;

    /**
     * Mocks components that use Internet.
     */
    @Before
    public void setup() throws IOException, TmcCoreException {
        settings = new ClientTmcSettings();
        settings.setServerAddress("http://mooc.fi/staging");
        settings.setUsername("chang");
        settings.setPassword("rajani");
        
        urlCommunicator = mock(UrlCommunicator.class);
        jsonParser = new TmcJsonParser(urlCommunicator, settings);
        rootFinder = new ProjectRootFinderStub(jsonParser);

        this.courseSubmitter = new CourseSubmitter(rootFinder, new ZipperStub(), urlCommunicator, jsonParser, settings);

        mockUrlCommunicator("/courses.json?api_version=7", ExampleJson.allCoursesExample);
        mockUrlCommunicator("courses/3.json?api_version=7", ExampleJson.courseExample);
        mockUrlCommunicator("courses/19.json?api_version=7", ExampleJson.noDeadlineCourseExample);
        mockUrlCommunicator("courses/21.json?api_version=7", ExampleJson.expiredCourseExample);
        mockUrlCommunicatorWithFile("https://tmc.mooc.fi/staging/exercises/285/submissions.json?api_version=7", ExampleJson.submitResponse);
        mockUrlCommunicatorWithFile("https://tmc.mooc.fi/staging/exercises/287/submissions.json?api_version=7", ExampleJson.pasteResponse);

        mockUrlCommunicatorWithFile("https://tmc.mooc.fi/staging/exercises/1228/submissions.json?api_version=7", ExampleJson.submitResponse);
        mockUrlCommunicatorWithFile("https://tmc.mooc.fi/staging/exercises/1228/submissions.json?api_version=7", ExampleJson.pasteResponse);

        realFinder = new ProjectRootFinder(new DefaultRootDetector(), jsonParser);
    }

    @Test
    public void testGetExerciseName() {
        final String path = "/home/test/ohpe-test/viikko_01";
        settings.setCurrentCourse(rootFinder.getCurrentCourse(path).or(new Course()));
        this.courseSubmitter = new CourseSubmitter(rootFinder, new ZipperStub(), urlCommunicator, jsonParser, settings);
        rootFinder.setReturnValue(path);
        String[] names = courseSubmitter.getExerciseName(path);
        assertEquals("viikko_01", names[names.length - 1]);
    }

    @Test
    public void testFindCourseByCorrectPath() throws IOException, TmcCoreException {
        final String path = "/home/kansio/toinen/c-demo/viikko_01";
        Optional<Course> course = realFinder.findCourseByPath(path.split(File.separator));
        assertEquals(7, course.get().getId());
        final String path2 = "/home/kansio/toinen/OLEMATON/viikko_01";
        Optional<Course> course2 = realFinder.findCourseByPath(path2.split(File.separator));
        assertFalse(course2.isPresent());
    }

    @Test
    public void testSubmitWithOneParam() throws IOException, ParseException, ExpiredException, IllegalArgumentException, ZipException, TmcCoreException {
        String testPath = "/home/test/2014-mooc-no-deadline/viikko1/viikko1-Viikko1_001.Nimi";
        settings.setCurrentCourse(rootFinder.getCurrentCourse(testPath).or(new Course()));
        this.courseSubmitter = new CourseSubmitter(rootFinder, new ZipperStub(), urlCommunicator, jsonParser, settings);
        rootFinder.setReturnValue(testPath);
        String submissionPath = "http://127.0.0.1:8080/submissions/1781.json?api_version=7";
        String result = courseSubmitter.submit(testPath);
        assertEquals(submissionPath, result);
    }

    @Test(expected = ExpiredException.class)
    public void testSubmitWithExpiredExercise() throws IOException, ParseException, ExpiredException, IllegalArgumentException, ZipException, TmcCoreException {
        String testPath = "/home/test/k2015-tira/viikko01/tira1.1";

        settings.setCurrentCourse(rootFinder.getCurrentCourse(testPath).or(new Course()));
        this.courseSubmitter = new CourseSubmitter(rootFinder, new ZipperStub(), urlCommunicator, jsonParser, settings);
        rootFinder.setReturnValue(testPath);
        String submissionPath = "http://127.0.0.1:8080/submissions/1781.json?api_version=7";
        String result = courseSubmitter.submit(testPath);
    }

    @Test
    public void submitWithPasteReturnsPasteUrl() throws IOException, ParseException, ExpiredException, IllegalArgumentException, ZipException, TmcCoreException {
        String testPath = "/home/test/2014-mooc-no-deadline/viikko1/viikko1-Viikko1_001.Nimi";
        settings.setCurrentCourse(rootFinder.getCurrentCourse(testPath).or(new Course()));
        this.courseSubmitter = new CourseSubmitter(rootFinder, new ZipperStub(), urlCommunicator, jsonParser, settings);
        rootFinder.setReturnValue(testPath);
        String pastePath = "https://tmc.mooc.fi/staging/paste/ynpw7_mZZGk3a9PPrMWOOQ";
        String result = courseSubmitter.submitPaste(testPath);
        assertEquals(pastePath, result);
    }
    
    @Test
    public void submitWithPasteAndCommentReturnsPasteUrl() throws IOException, ParseException, ExpiredException, IllegalArgumentException, ZipException, TmcCoreException {
        String testPath = "/home/test/2014-mooc-no-deadline/viikko1/viikko1-Viikko1_001.Nimi";
        settings.setCurrentCourse(rootFinder.getCurrentCourse(testPath).or(new Course()));
        this.courseSubmitter = new CourseSubmitter(rootFinder, new ZipperStub(), urlCommunicator, jsonParser, settings);
        rootFinder.setReturnValue(testPath);
        String pastePath = "https://tmc.mooc.fi/staging/paste/ynpw7_mZZGk3a9PPrMWOOQ";
        String result = courseSubmitter.submitPasteWithComment(testPath, "Commentti");
        assertEquals(pastePath, result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void submitWithPasteFromBadPathThrowsException() throws IOException, ParseException, ExpiredException, IllegalArgumentException, ZipException, TmcCoreException {
        String testPath = "/home/test/2014-mooc-no-deadline/viikko1/feikeintehtava";
        settings.setCurrentCourse(rootFinder.getCurrentCourse(testPath).or(new Course()));
        this.courseSubmitter = new CourseSubmitter(rootFinder, new ZipperStub(), urlCommunicator, jsonParser, settings);
        rootFinder.setReturnValue(testPath);
        String result = courseSubmitter.submit(testPath);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubmitWithNonexistentExercise() throws IOException, ParseException, ExpiredException, IllegalArgumentException, ZipException, TmcCoreException {
        String testPath = "/home/test/2014-mooc-no-deadline/viikko1/feikkitehtava";
        settings.setCurrentCourse(rootFinder.getCurrentCourse(testPath).or(new Course()));
        this.courseSubmitter = new CourseSubmitter(rootFinder, new ZipperStub(), urlCommunicator, jsonParser, settings);
        rootFinder.setReturnValue(testPath);
        String result = courseSubmitter.submit(testPath);
    }

    @Test(expected = IllegalArgumentException.class)
    public void submitWithNonExistentCourseThrowsException() throws IOException, ParseException, ExpiredException, IllegalArgumentException, ZipException, TmcCoreException {
        String testPath = "/home/test/2013_FEIKKIKURSSI/viikko_01/viikko1-Viikko1_001.Nimi";
        settings.setCurrentCourse(rootFinder.getCurrentCourse(testPath).or(new Course()));
        this.courseSubmitter = new CourseSubmitter(rootFinder, new ZipperStub(), urlCommunicator, jsonParser, settings);
        rootFinder.setReturnValue(testPath);
        String result = courseSubmitter.submit(testPath);
    }

    private void mockUrlCommunicator(String pieceOfUrl, String returnValue) throws IOException, TmcCoreException {
        HttpResult fakeResult = new HttpResult(returnValue, 200, true);
        Mockito.when(urlCommunicator.makeGetRequest(Mockito.contains(pieceOfUrl),
                                Mockito.anyString()))
                .thenReturn(fakeResult);
        Mockito.when(urlCommunicator
                .makeGetRequestWithAuthentication(Mockito.contains(pieceOfUrl)))
                .thenReturn(fakeResult);
    }

    @SuppressWarnings("unchecked")
    private void mockUrlCommunicatorWithFile(String url, String returnValue) throws IOException, TmcCoreException {
        HttpResult fakeResult = new HttpResult(returnValue, 200, true);
        Mockito.when(urlCommunicator.makePostWithFile(Mockito.any(FileBody.class),
                                Mockito.contains(url), Mockito.any(Map.class)))
                .thenReturn(fakeResult);
        Mockito.when(urlCommunicator.makePostWithFileAndParams(Mockito.any(FileBody.class),
                                Mockito.contains(url), Mockito.any(Map.class), Mockito.any(Map.class)))
                .thenReturn(fakeResult);
        
    }
}
