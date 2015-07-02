package hy.tmc.core.testhelpers.builders;

import hy.tmc.core.domain.Exercise;
import java.util.ArrayList;
import java.util.List;


public class ExerciseBuilder {
    
    private List<Exercise> exercises;
    
    public ExerciseBuilder() {
        this.exercises = new ArrayList<>();
    }
    
    public ExerciseBuilder withExercise(String name, int id, String checksum) {
        Exercise exercise = new Exercise();
        exercise.setName(name);
        exercise.setId(id);
        exercise.setChecksum(checksum);
        exercises.add(exercise);
        return this;
    }
    
    public List<Exercise> build() {
        return this.exercises;
    }
}
