package fi.helsinki.cs.tmc.core.testhelpers.builders;

import fi.helsinki.cs.tmc.core.domain.Exercise;

import java.util.ArrayList;
import java.util.List;

public class ExerciseBuilder {

    private final List<Exercise> exercises;

    public ExerciseBuilder() {
        this.exercises = new ArrayList<>();
    }

    public ExerciseBuilder withExercise(String name, int id, String checksum) {
        return withExercise(name, id, checksum, null);
    }

    public ExerciseBuilder withExercise(String name, int id, String checksum, String courseName) {
        Exercise exercise = new Exercise();
        exercise.setName(name);
        exercise.setId(id);
        exercise.setChecksum(checksum);
        exercise.setCourseName(courseName);
        exercises.add(exercise);
        return this;
    }

    public List<Exercise> build() {
        return this.exercises;
    }
}
