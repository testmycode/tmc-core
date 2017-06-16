package fi.helsinki.cs.tmc.core.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Theme implements Serializable{
    private List<Exercise> exercises;
    private boolean unlocked = false;
    private String name;
    private List<Skill> skills;

    public Theme(String name) {
        this.name = name;
        this.exercises = new ArrayList<>();
        this.skills = new ArrayList<>();
    }

    public List<Exercise> getExercises() {
        return exercises;
    }

    public List<Skill> getSkills() {
        return skills;
    }

    public void setSkills(List<Skill> skills) {
        this.skills = skills;
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
        return this.name.equals(exercise.getName().split("-")[0]);
    }

    public void addExercise(Exercise exercise) {
        exercises.add(exercise);
    }

    public void addSkill(Skill skill) {
        skills.add(skill);
    }
}
