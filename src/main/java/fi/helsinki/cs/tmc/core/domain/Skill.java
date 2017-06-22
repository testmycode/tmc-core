package fi.helsinki.cs.tmc.core.domain;

import java.io.Serializable;
import java.util.List;

public class Skill implements Serializable{
    private List<Exercise> exercises;
    private String name;
    private double percentage;
    public double mastery;
    private int week;

    public Skill(String name, int week) {
        this.name = name;
        percentage = 0.0;
        mastery = 90.0;
        this.week = week;
    }

    public void setExercises(List<Exercise> exercises) {
        this.exercises = exercises;
    }

    public String getName() {
        return name;
    }

    public double getPercentage() {
        return percentage;
    }

    public int getWeek() {
        return week;
    }

    public boolean isMastered() {
        return percentage >= mastery;
    }

    public void setWeek(int week) {
        this.week = week;
    }
}
