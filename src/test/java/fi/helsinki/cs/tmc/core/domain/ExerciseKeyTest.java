package fi.helsinki.cs.tmc.core.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class ExerciseKeyTest {

    @Test
    public void constructorSetsCourseName() {
        ExerciseKey key = new ExerciseKey("course", "exercise");
        assertEquals("course", key.courseName);
    }

    @Test
    public void constructorSetsExerciseName() {
        ExerciseKey key = new ExerciseKey("course", "exercise");
        assertEquals("exercise", key.exerciseName);
    }

    @Test
    public void equalsToKeyWithSameParams() {
        ExerciseKey key1 = new ExerciseKey("a", "a");
        ExerciseKey key2 = new ExerciseKey("a", "a");
        assertEquals(key1, key2);
        assertEquals(key1.hashCode(), key2.hashCode());
    }

    @Test
    public void notEqualsWithDifferentExercise() {
        ExerciseKey key1 = new ExerciseKey("a", "b");
        ExerciseKey key2 = new ExerciseKey("a", "a");
        assertNotEquals(key1, key2);
        assertNotEquals(key1.hashCode(), key2.hashCode());
    }

    @Test
    public void notEqualsWithDifferentCourse() {
        ExerciseKey key1 = new ExerciseKey("b", "a");
        ExerciseKey key2 = new ExerciseKey("a", "a");
        assertNotEquals(key1, key2);
        assertNotEquals(key1.hashCode(), key2.hashCode());
    }

    @Test
    public void hasProperToString() {
        ExerciseKey key = new ExerciseKey("course", "exercise");
        assertEquals("course/exercise", key.toString());
    }
}
