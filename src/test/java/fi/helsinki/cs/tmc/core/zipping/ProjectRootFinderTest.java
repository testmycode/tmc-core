package fi.helsinki.cs.tmc.core.zipping;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.core.CoreTestSettings;
import fi.helsinki.cs.tmc.core.communication.TmcApi;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;

import com.google.common.base.Optional;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ProjectRootFinderTest {

    private ProjectRootFinder finder;
    private String fakeName = "2014-mooc-no-deadline";
    private String otherFakeName = "2013-tira";
    private CoreTestSettings settings;
    private TmcApi parser;

    @Before
    public void setUp() throws IOException, TmcCoreException {
        settings = new CoreTestSettings();
        settings.setUsername("chang");
        settings.setPassword("paras");

        parser = Mockito.mock(TmcApi.class);
        Mockito.when(parser.getCourses()).thenReturn(setupFakeCourses());
        finder = new ProjectRootFinder(parser);
    }

    private List<Course> setupFakeCourses() {
        Course fakeCourse = new Course();
        fakeCourse.setName(fakeName);

        Course secondCourse = new Course();
        secondCourse.setName(otherFakeName);

        List<Course> courses = new ArrayList<>();
        courses.add(fakeCourse);
        courses.add(secondCourse);
        return courses;
    }

    @Test
    public void testGetRootDirectoryFromSame() {
        Path path
                = Paths.get("testResources", "local-test-course", "successExercise");
        Optional<Path> root = finder.getRootDirectory(path);
        assertTrue(root.isPresent());
        assertTrue(root.get().endsWith(path));
    }

    @Test
    public void findsDeepRoot() {
        Optional<Path> root
                = finder.getRootDirectory(
                        Paths.get(
                                "testResources",
                                "local-test-course",
                                "successExercise",
                                "src"));
        assertTrue(root.isPresent());
        assertTrue(
                root
                .get()
                .endsWith(
                        Paths.get(
                                "testResources",
                                "local-test-course",
                                "successExercise")));
    }

    @Test
    public void doesntFindRootWhenNoPom() {
        Optional<Path> root
                = finder.getRootDirectory(
                        Paths.get(
                                "testResources",
                                "local-test-course",
                                "successExercise",
                                "src"));
        assertTrue(root.isPresent());
        assertTrue(
                root
                .get()
                .endsWith(
                        Paths.get(
                                "testResources",
                                "local-test-course",
                                "successExercise")));
    }

    @Test
    public void doesntFindRootWhenNotAnExercise() {
        Path notAnExercise
                = Paths.get("testResources", "local-test-course", "NotAnExercise");
        Optional<Path> root = finder.getRootDirectory(notAnExercise);
        if (!root.isPresent()) {
            return;
        }
        assertFalse(root.get().endsWith(notAnExercise));
    }

    @Test
    public void getsCourseNameFromPath() throws IOException, TmcCoreException {
        String[] paths = new String[3];
        paths[0] = "paras";
        paths[1] = "path";
        paths[2] = "2013-tira";
        Optional<Course> course = finder.findCourseByPath(paths);
        assertEquals(otherFakeName, course.get().getName());
    }

    @Test
    public void getsCurrentCourse() throws IOException, TmcCoreException {
        Optional<Course> course
                = finder.getCurrentCourse(
                        Paths.get("path", "that", "contains", "course").toString()
                        + File.separatorChar
                        + fakeName);
        assertEquals(fakeName, course.get().getName());
    }
}
