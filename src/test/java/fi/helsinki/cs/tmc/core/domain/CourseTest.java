package fi.helsinki.cs.tmc.core.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CourseTest {

    private final int id = 7;
    private final String name = "ankka";

    private Course course;
    private List<Exercise> exercises;
    private Exercise ex;

    @Before
    public void setUp() {
        course = new Course();
        course.setId(id);
        course.setName(name);
        course.setDetailsUrl(URI.create("http://example.com/"));

        exercises = new ArrayList<>();
        ex = new Exercise();
        ex.setName("test");
        ex.setId(2);
        exercises.add(ex);

        course.setExercises(exercises);
    }

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
        assertEquals(URI.create("http://example.com/"), course.getDetailsUrl());
    }

    @Test
    public void testSetDetailsUrl() {
        course.setDetailsUrl(URI.create("http://cs.helsinki.fi"));
        assertEquals(URI.create("http://cs.helsinki.fi"), course.getDetailsUrl());
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

    @Test
    public void courseExercisesAreDividedIntoThemesCorrectly() {
        exercises = new ArrayList<>();
        exercises.add(new Exercise("viikko1-testi"));
        exercises.add(new Exercise("viikko2-testi"));
        exercises.add(new Exercise("viikko3-testi"));
        course.setExercises(exercises);
        course.generateThemes();
        assertEquals(3,course.getExercises().size());
    }

    @Test
    public void returnExercisesByThemeTest() {
        exercises = new ArrayList<>();
        exercises.add(new Exercise("viikko1-testi"));
        exercises.add(new Exercise("viikko1-testi"));
        exercises.add(new Exercise("viikko3-testi"));
        course.setExercises(exercises);
        course.generateThemes();
        assertEquals(2, course.getExercisesByTheme("viikko1").size());
        assertEquals(1, course.getExercisesByTheme("viikko3").size());
    }


}
