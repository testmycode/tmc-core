package fi.helsinki.cs.tmc.core.domain;

import java.util.Objects;

/**
 * A pair (course name, exercise name).
 */
public final class ExerciseKey {
    public final String courseName;
    public final String exerciseName;

    public ExerciseKey(String courseName, String exerciseName) {
        this.courseName = courseName;
        this.exerciseName = exerciseName;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        ExerciseKey that = (ExerciseKey) other;
        return Objects.equals(courseName, that.courseName)
                && Objects.equals(exerciseName, that.exerciseName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseName, exerciseName);
    }

    @Override
    public String toString() {
        return courseName + "/" + exerciseName;
    }
}
