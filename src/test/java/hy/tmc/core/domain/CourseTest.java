package hy.tmc.core.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import hy.tmc.core.domain.Exercise;
import hy.tmc.core.domain.Course;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CourseTest {

    private Course course;
    private final int id = 7;
    private final String name = "ankka";
    private List<Exercise> exercises;
    private Exercise ex;

    @BeforeClass
    public static void setUpClass() {}

    @AfterClass
    public static void tearDownClass() {}

    /**
     * Setups an Course object for testing.
     */
    @Before
    public void setUp() {
        course = new Course();
        course.setId(id);
        course.setName(name);
        course.setDetailsUrl("http://mooc.fi/");

        exercises = new ArrayList<>();
        ex = new Exercise();
        ex.setName("test");
        ex.setId(2);
        exercises.add(ex);

        course.setExercises(exercises);
    }

    @After
    public void tearDown() {}

    @Test
    public void testGetName() {
        assertEquals(name, course.getName());
    }

    @Test
    public void testSetName() {
        course.setName("höyrykaivuri");
        assertEquals("höyrykaivuri", course.getName());
    }

    @Test
    public void testGetId() {
        assertEquals(id, course.getId());
    }

    @Test
    public void testSetId() {
        course.setId(888);
        assertEquals(888, course.getId());
    }

    @Test
    public void testGetExercises() {
        assertEquals(exercises, course.getExercises());
    }

    @Test
    public void testSetExercises() {
        List<Exercise> newExercises = new ArrayList<>();
        course.setExercises(newExercises);
        assertEquals(newExercises, course.getExercises());
    }

    @Test
    public void testGetDetailsUrl() {
        assertEquals("http://mooc.fi/", course.getDetailsUrl());
    }

    @Test
    public void testSetDetailsUrl() {
        course.setDetailsUrl("http://cs.helsinki.fi");
        assertEquals("http://cs.helsinki.fi", course.getDetailsUrl());
    }

    @Test
    public void equalsWorksViaId() {
        Course c1 = new Course();
        Course c2 = new Course();
        c1.setId(1);
        c2.setId(1);

        assertTrue(c1.equals(c2));
    }

    @Test
    public void hashCodeGeneratedFromId() {
        Set<Course> courses = new HashSet<>();
        Course c1 = new Course();
        c1.setId(1);
        courses.add(c1);

        Course c2 = new Course();
        c2.setId(1);
        assertTrue(courses.contains(c2));
    }
}
