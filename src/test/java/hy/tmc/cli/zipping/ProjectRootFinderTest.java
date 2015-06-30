package hy.tmc.cli.zipping;

import com.google.common.base.Optional;
import hy.tmc.cli.backend.communication.TmcJsonParser;
import hy.tmc.cli.configuration.ClientData;
import hy.tmc.cli.configuration.ConfigHandler;
import hy.tmc.cli.domain.Course;
import hy.tmc.core.exceptions.ProtocolException;
import java.io.IOException;
import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertFalse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(TmcJsonParser.class)
public class ProjectRootFinderTest {

    ProjectRootFinder finder;
    String fakeName = "2014-mooc-no-deadline";
    String otherFakeName = "2013-tira";

    @Before
    public void setUp() throws IOException, ProtocolException {
        ClientData.setUserData("chang", "paras");

        finder = new ProjectRootFinder(new DefaultRootDetector());

        PowerMockito.mockStatic(TmcJsonParser.class);

        List<Course> courses = setupFakeCourses();
        PowerMockito
                .when(TmcJsonParser.getCourses(new ConfigHandler()
                        .readCoursesAddress()))
                .thenReturn(courses);
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

    @After
    public void tearDown() {
        ClientData.clearUserData();
    }

    @Test
    public void testGetRootDirectoryFromSame() {
        Optional<Path> root = finder.getRootDirectory(Paths.get("testResources/mockProject/root"));
        assertEquals("testResources/mockProject/root", root.get().toString());
    }

    @Test
    public void testGetRootDirectoryFromSame2() {
        Optional<Path> root = finder.getRootDirectory(Paths.get("testResources/noyml/rootWithoutYml"));
        assertEquals("testResources/noyml/rootWithoutYml", root.get().toString());
    }

    @Test
    public void findsDeepRoot() {
        Optional<Path> root = finder.getRootDirectory(Paths.get("testResources/2013_ohpeJaOhja/viikko1/Viikko1_002.HeiMaailma/src"));
        assertEquals("testResources/2013_ohpeJaOhja/viikko1/Viikko1_002.HeiMaailma", root.get().toString());
    }

    @Test
    public void doesntFindRootWhenNoPom() {
        Optional<Path> root = finder.getRootDirectory(Paths.get("testResources/2013_ohpeJaOhja"));
        assertFalse(root.isPresent());
    }

    @Test
    public void getsCourseNameFromPath() throws IOException, ProtocolException {
        String[] paths = new String[3];
        paths[0] = "paras";
        paths[1] = "path";
        paths[2] = "2013-tira";
        Optional<Course> course = finder.findCourseByPath(paths);
        assertEquals(otherFakeName, course.get().getName());
    }

    @Test
    public void getsCurrentCourse() throws IOException, ProtocolException {
        Optional<Course> course = finder.getCurrentCourse("path/that/contains/course/" + fakeName);
        assertEquals(fakeName, course.get().getName());
    }
}
