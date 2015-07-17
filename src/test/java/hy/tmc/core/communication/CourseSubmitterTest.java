package hy.tmc.core.communication;

import com.google.common.base.Optional;
import fi.helsinki.cs.tmc.langs.io.zip.StudentFileAwareZipper;
import fi.helsinki.cs.tmc.langs.io.zip.Zipper;
import fi.helsinki.cs.tmc.langs.util.TaskExecutorImpl;
import static org.junit.Assert.assertEquals;

import org.mockito.Mockito;

import hy.tmc.core.CoreTestSettings;
import hy.tmc.core.domain.Course;
import hy.tmc.core.exceptions.ExpiredException;
import hy.tmc.core.exceptions.TmcCoreException;
import hy.tmc.core.testhelpers.ExampleJson;
import hy.tmc.core.testhelpers.ProjectRootFinderStub;
import hy.tmc.core.zipping.ProjectRootFinder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.Map;
import net.lingala.zip4j.exception.ZipException;
import static org.junit.Assert.assertFalse;
import org.junit.Before;
import org.junit.Test;
import static org.powermock.api.mockito.PowerMockito.mock;


public class CourseSubmitterTest {

    private ExerciseSubmitter courseSubmitter;
    private UrlCommunicator urlCommunicator;
    private TmcJsonParser jsonParser;
    private ProjectRootFinderStub rootFinder;
    private ProjectRootFinder realFinder;
    private CoreTestSettings settings;
    private Zipper zipper;
    
    String v = File.separator;

    /**
     * Mocks components that use Internet.
     */
    @Before
    public void setup() throws IOException, TmcCoreException {
        settings = new CoreTestSettings();
        settings.setServerAddress("http://mooc.fi/staging");
        settings.setUsername("chang");
        settings.setPassword("rajani");
        
        urlCommunicator = mock(UrlCommunicator.class);
        jsonParser = new TmcJsonParser(urlCommunicator, settings);
        rootFinder = new ProjectRootFinderStub(jsonParser);
        zipper = Mockito.mock(StudentFileAwareZipper.class);

        Mockito.when(zipper.zip(Mockito.any(Path.class))).thenReturn(new byte[100]);
        
        this.courseSubmitter = new ExerciseSubmitter(rootFinder, zipper, urlCommunicator, jsonParser, settings);

        mockUrlCommunicator("/courses.json?api_version=7", ExampleJson.allCoursesExample);
        mockUrlCommunicator("courses/3.json?api_version=7", ExampleJson.courseExample);
        mockUrlCommunicator("courses/19.json?api_version=7", ExampleJson.noDeadlineCourseExample);
        mockUrlCommunicator("courses/21.json?api_version=7", ExampleJson.expiredCourseExample);
        mockUrlCommunicatorWithFile("https://tmc.mooc.fi/staging/exercises/285/submissions.json?api_version=7", ExampleJson.submitResponse);
        mockUrlCommunicatorWithFile("https://tmc.mooc.fi/staging/exercises/287/submissions.json?api_version=7", ExampleJson.pasteResponse);

        mockUrlCommunicatorWithFile("https://tmc.mooc.fi/staging/exercises/1228/submissions.json?api_version=7", ExampleJson.submitResponse);
        mockUrlCommunicatorWithFile("https://tmc.mooc.fi/staging/exercises/1228/submissions.json?api_version=7", ExampleJson.pasteResponse);

        realFinder = new ProjectRootFinder(new TaskExecutorImpl(), jsonParser);
    }

    @Test
    public void testGetExerciseName() {
        final String path = v+"home"+v+"test"+v+"ohpe-test"+v+"viikko_01";
        settings.setCurrentCourse(rootFinder.getCurrentCourse(path).or(new Course()));
        this.courseSubmitter = new ExerciseSubmitter(rootFinder, zipper, urlCommunicator, jsonParser, settings);
        rootFinder.setReturnValue(path);
        String[] names = courseSubmitter.getExerciseName(path);
        assertEquals("viikko_01", names[names.length - 1]);
    }

    @Test
    public void testFindCourseByCorrectPath() throws IOException, TmcCoreException {
        final String path = v+"home"+v+"kansio"+v+"toinen"+v+"c-demo"+v+"viikko_01";
        Optional<Course> course = realFinder.findCourseByPath(path.split("\\" + File.separator));
        assertEquals(7, course.get().getId());
        final String path2 = v+"home"+v+"kansio"+v+"toinen"+v+"OLEMATON"+v+"viikko_01";
        Optional<Course> course2 = realFinder.findCourseByPath(path2.split("\\" + File.separator));
        assertFalse(course2.isPresent());
    }

    @Test
    public void testSubmitWithOneParam() throws IOException, ParseException, ExpiredException, IllegalArgumentException, ZipException, TmcCoreException {
        String testPath = v+"home"+v+"test"+v+"2014-mooc-no-deadline"+v+"viikko1"+v+"viikko1-Viikko1_001.Nimi";
        settings.setCurrentCourse(rootFinder.getCurrentCourse(testPath).or(new Course()));
        this.courseSubmitter = new ExerciseSubmitter(rootFinder, zipper, urlCommunicator, jsonParser, settings);
        rootFinder.setReturnValue(testPath);
        String submissionPath = "http://127.0.0.1:8080/submissions/1781.json?api_version=7";
        String result = courseSubmitter.submit(testPath);
        assertEquals(submissionPath, result);
    }

    @Test(expected = ExpiredException.class)
    public void testSubmitWithExpiredExercise() throws IOException, ParseException, ExpiredException, IllegalArgumentException, ZipException, TmcCoreException {
        String testPath = v+"home"+v+"test"+v+"k2015-tira"+v+"viikko01"+v+"tira1.1";

        settings.setCurrentCourse(rootFinder.getCurrentCourse(testPath).or(new Course()));
        this.courseSubmitter = new ExerciseSubmitter(rootFinder, zipper, urlCommunicator, jsonParser, settings);
        rootFinder.setReturnValue(testPath);
        String submissionPath = "http://127.0.0.1:8080/submissions/1781.json?api_version=7";
        String result = courseSubmitter.submit(testPath);
    }

    @Test
    public void submitWithPasteReturnsPasteUrl() throws IOException, ParseException, ExpiredException, IllegalArgumentException, ZipException, TmcCoreException {
        String testPath = v+"home"+v+"test"+v+"2014-mooc-no-deadline"+v+"viikko1"+v+"viikko1-Viikko1_001.Nimi";
        settings.setCurrentCourse(rootFinder.getCurrentCourse(testPath).or(new Course()));
        this.courseSubmitter = new ExerciseSubmitter(rootFinder, zipper, urlCommunicator, jsonParser, settings);
        rootFinder.setReturnValue(testPath);
        String pastePath = "https://tmc.mooc.fi/staging/paste/ynpw7_mZZGk3a9PPrMWOOQ";
        String result = courseSubmitter.submitPaste(testPath);
        assertEquals(pastePath, result);
    }
    
    @Test
    public void submitWithPasteAndCommentReturnsPasteUrl() throws IOException, ParseException, ExpiredException, IllegalArgumentException, ZipException, TmcCoreException {
        String testPath = v+"home"+v+"test"+v+"2014-mooc-no-deadline"+v+"viikko1"+v+"viikko1-Viikko1_001.Nimi";
        settings.setCurrentCourse(rootFinder.getCurrentCourse(testPath).or(new Course()));
        this.courseSubmitter = new ExerciseSubmitter(rootFinder, zipper, urlCommunicator, jsonParser, settings);
        rootFinder.setReturnValue(testPath);
        String pastePath = "https://tmc.mooc.fi/staging/paste/ynpw7_mZZGk3a9PPrMWOOQ";
        String result = courseSubmitter.submitPasteWithComment(testPath, "Commentti");
        assertEquals(pastePath, result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void submitWithPasteFromBadPathThrowsException() throws IOException, ParseException, ExpiredException, IllegalArgumentException, ZipException, TmcCoreException {
        String testPath = v+"home"+v+"test"+v+"2014-mooc-no-deadline"+v+"viikko1"+v+"feikeintehtava";
        settings.setCurrentCourse(rootFinder.getCurrentCourse(testPath).or(new Course()));
        this.courseSubmitter = new ExerciseSubmitter(rootFinder, zipper, urlCommunicator, jsonParser, settings);
        rootFinder.setReturnValue(testPath);
        String result = courseSubmitter.submit(testPath);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubmitWithNonexistentExercise() throws IOException, ParseException, ExpiredException, IllegalArgumentException, ZipException, TmcCoreException {
        String testPath = v+"home"+v+"test"+v+"2014-mooc-no-deadline"+v+"viikko1"+v+"feikkitehtava";
        settings.setCurrentCourse(rootFinder.getCurrentCourse(testPath).or(new Course()));
        this.courseSubmitter = new ExerciseSubmitter(rootFinder, zipper, urlCommunicator, jsonParser, settings);
        rootFinder.setReturnValue(testPath);
        String result = courseSubmitter.submit(testPath);
    }

    @Test(expected = IllegalArgumentException.class)
    public void submitWithNonExistentCourseThrowsException() throws IOException, ParseException, ExpiredException, IllegalArgumentException, ZipException, TmcCoreException {
        String testPath = v+"home"+v+"test"+v+"2013_FEIKKIKURSSI"+v+"viikko_01"+v+"viikko1-Viikko1_001.Nimi";
        settings.setCurrentCourse(rootFinder.getCurrentCourse(testPath).or(new Course()));
        this.courseSubmitter = new ExerciseSubmitter(rootFinder, zipper, urlCommunicator, jsonParser, settings);
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
        Mockito.when(urlCommunicator.makePostWithByteArray(
                                Mockito.contains(url), Mockito.any(byte[].class), Mockito.any(Map.class), Mockito.any(Map.class)))
                .thenReturn(fakeResult);
        /*Mockito.when(urlCommunicator.makePostWithFileAndParams(Mockito.any(FileBody.class),
                                Mockito.contains(url), Mockito.any(Map.class), Mockito.any(Map.class)))
                .thenReturn(fakeResult);*/
        
    }
}
