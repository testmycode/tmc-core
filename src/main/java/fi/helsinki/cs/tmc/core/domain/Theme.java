package fi.helsinki.cs.tmc.core.domain;

import java.util.List;

/**
 * Created by markovai on 29.5.2017.
 */
public class Theme {
    private List<Exercise> exercises;
    private boolean unlocked = false;
    private String name;

    public Theme(String name) {
        this.name = name;
    }

    public List<Exercise> getExercises() {
        return exercises;
    }

    public void setExercises(List<Exercise> exercises) {
        this.exercises = exercises;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public boolean shouldContain(Exercise exercise) {
        return exercise.getName().contains(this.name);
    }

}
