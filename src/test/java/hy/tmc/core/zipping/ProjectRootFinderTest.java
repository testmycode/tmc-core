package hy.tmc.core.zipping;

import com.google.common.base.Optional;
import hy.tmc.core.communication.TmcJsonParser;
import hy.tmc.core.CoreTestSettings;
import hy.tmc.core.domain.Course;
import hy.tmc.core.exceptions.TmcCoreException;
import java.io.File;
import java.io.IOException;
import static org.junit.Assert.assertEquals;

import org.junit.Before;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.mockito.Mockito;

public class ProjectRootFinderTest {

    ProjectRootFinder finder;
    String fakeName = "2014-mooc-no-deadline";
    String otherFakeName = "2013-tira";
    CoreTestSettings settings;
    TmcJsonParser parser;
    String v = File.separator;

    @Before
    public void setUp() throws IOException, TmcCoreException {
        settings = new CoreTestSettings();
        settings.setUsername("chang");
        settings.setPassword("paras");
        
        parser = Mockito.mock(TmcJsonParser.class);   
        Mockito
                .when(parser.getCourses())
                .thenReturn(setupFakeCourses());
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
        Optional<Path> root = finder.getRootDirectory(Paths.get("testResources"+v+"mockProject"+v+"root"));
        assertEquals("testResources"+v+"mockProject"+v+"root", root.get().toString());
    }

    @Test
    public void testGetRootDirectoryFromSame2() {
        Optional<Path> root = finder.getRootDirectory(Paths.get("testResources"+v+"noyml"+v+"rootWithoutYml"));
        assertEquals("testResources"+v+"noyml"+v+"rootWithoutYml", root.get().toString());
        Path path = Paths.get("testResources","2013_ohpeJaOhja","viikko1","Viikko1_002.HeiMaailma");
        assertTrue(root.isPresent());
        assertTrue(root.get().endsWith(path));
    }

    @Test
    public void findsDeepRoot() {
        Optional<Path> root = finder.getRootDirectory(Paths.get("testResources"+v+"2013_ohpeJaOhja"+v+"viikko1"+v+"Viikko1_002.HeiMaailma"+v+"src"));
        assertEquals("testResources"+v+"2013_ohpeJaOhja"+v+"viikko1"+v+"Viikko1_002.HeiMaailma", root.get().toString());
    }

    @Test
    public void doesntFindRootWhenNoPom() {
        Optional<Path> root = finder.getRootDirectory(Paths.get("testResources","2013_ohpeJaOhja","viikko1","Viikko1_002.HeiMaailma","src"));
        assertTrue(root.isPresent());
        assertTrue(root.get().endsWith(Paths.get("testResources","2013_ohpeJaOhja","viikko1","Viikko1_002.HeiMaailma")));
    }

    @Test
    public void doesntFindRootWhenNotAnExercise() {
        Optional<Path> root = finder.getRootDirectory(Paths.get("testResources","2013_ohpeJaOhja", "viikko1", "NotAnExercise"));
        assertFalse(root.isPresent());
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
        Optional<Course> course = finder.getCurrentCourse(Paths.get("path","that","contains","course").toString() + File.separatorChar + fakeName);
        assertEquals(fakeName, course.get().getName());
    }
}
