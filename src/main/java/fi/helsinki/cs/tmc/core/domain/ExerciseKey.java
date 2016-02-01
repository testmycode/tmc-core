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
    public boolean equals(Object obj) {
        // TODO: nullcheck + rewrite?
        if (obj instanceof ExerciseKey) {
            ExerciseKey that = (ExerciseKey) obj;
            return Objects.equals(this.courseName, that.courseName)
                    && Objects.equals(this.exerciseName, that.exerciseName);
        } else {
            return false;
        }
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
