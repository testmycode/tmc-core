package fi.helsinki.cs.tmc.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.core.CoreTestSettings;
import fi.helsinki.cs.tmc.core.communication.TmcApi;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.exceptions.InvalidExerciseDirectoryException;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;

import com.google.common.base.Optional;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ProjectRootFinderTest {

    private ProjectRootFinder finder;
    private String courseName1 = "courseName1";
    private String courseName2 = "courseName2";
    private CoreTestSettings settings;
    private TmcApi tmcApi;

    @Before
    public void setUp() throws IOException, TmcCoreException {
        settings = new CoreTestSettings();
        settings.setUsername("username");
        settings.setPassword("password");

        tmcApi = Mockito.mock(TmcApi.class);
        Mockito.when(tmcApi.getCourses()).thenReturn(setupFakeCourses());
        finder = new ProjectRootFinder(tmcApi);
    }

    private List<Course> setupFakeCourses() {
        Course fakeCourse = new Course();
        fakeCourse.setName(courseName1);

        Course secondCourse = new Course();
        secondCourse.setName(courseName2);

        List<Course> courses = new ArrayList<>();
        courses.add(fakeCourse);
        courses.add(secondCourse);
        return courses;
    }

    @Test
    public void testGetRootDirectoryFromSame() throws InvalidExerciseDirectoryException {
        Path path = Paths.get("src/test/resources", "local-test-course", "successExercise");
        Path root = finder.getExerciseRoot(path);
        assertTrue(root.endsWith(path));
    }

    @Test
    public void findsDeepExerciseRoot() throws InvalidExerciseDirectoryException {
        Path root =
                finder.getExerciseRoot(
                        Paths.get("src/test/resources",
                                "local-test-course",
                                "successExercise",
                                "src"));
        assertTrue(
                root.endsWith(
                        Paths.get(
                                "src/test/resources",
                                "local-test-course",
                                "successExercise")));
    }

    @Test(expected = InvalidExerciseDirectoryException.class)
    public void failsToFindExerciseRootForNonProjectPath() throws InvalidExerciseDirectoryException, IOException {
        Path tempDir = Files.createTempDirectory("TMC-Temp").resolve("IsNotProject");
        finder.getExerciseRoot(
                tempDir
        );
        Files.deleteIfExists(tempDir);
    }

    @Test
    public void getsCourseForCourseRootPath() throws IOException, TmcCoreException {
        Optional<Course> course = finder.getCourse(Paths.get("this", "is", courseName2));
        assertEquals(courseName2, course.get().getName());
    }

    @Test
    public void getsCourseForExerciseSubPath() throws IOException, TmcCoreException {
        Optional<Course> course = finder.getCourse(
                Paths.get(
                        "path",
                        "that",
                        "contains",
                        "course",
                        courseName1,
                        "and",
                        "more")
        );
        assertEquals(courseName1, course.get().getName());
    }

    @Test
    public void failsToGetCourseForInvalidPath() throws IOException, TmcCoreException {
        Optional<Course> course = finder.getCourse(
                Paths.get(
                        "path",
                        "that",
                        "does",
                        "not",
                        "contain",
                        "course")
        );
    }
}
