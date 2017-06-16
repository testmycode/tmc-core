package fi.helsinki.cs.tmc.core.domain;

import java.io.Serializable;
import java.util.List;

public class Skill implements Serializable{
    private List<Exercise> exercises;
    private String name;
    private double percentage;
    public double mastery;
    private String themeName;

    public Skill(String name) {
        this.name = name;
        percentage = 0.0;
        mastery = 90.0;
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

    public String getThemeName() {
        return themeName;
    }

    public boolean isMastered() {
        return percentage >= mastery;
    }

    public void setThemeName(String themeName) {
        this.themeName = themeName;
    }
}
