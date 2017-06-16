package fi.helsinki.cs.tmc.core.domain;

import java.util.List;

public class Skill {
    private List<Exercise> exercises;
    private Theme theme;
    private String name;
    private double percentage;
    public double mastery;

    public Skill(String name) {
        this.name = name;
        percentage = 0.0;
        mastery = 90.0;
    }

    public void incrementPercentage(double amount) {
        this.percentage += amount;
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
    }

    public void setExercises(List<Exercise> exercises) {
        this.exercises = exercises;
    }

    public String getName() {
        return name;
    }

    public Theme getTheme() {
        return theme;
    }

    public double getPercentage() {
        return percentage;
    }
}
