package fi.helsinki.cs.tmc.core.domain;

import com.google.gson.annotations.SerializedName;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class Course {

    private int id;
    private String name;
    private String title;
    private String description;

    private List<Exercise> exercises;
    private List<Skill> skills;

    private int weekCount;

    @SerializedName("details_url")
    private URI detailsUrl;

    @SerializedName("unlock_url")
    private URI unlockUrl;

    @SerializedName("comet_url")
    private URI cometUrl;

    public URI getReviewsUrl() {
        return reviewsUrl;
    }

    public void setReviewsUrl(URI reviewsUrl) {
        this.reviewsUrl = reviewsUrl;
    }

    @SerializedName("spyware_urls")
    private List<URI> spywareUrls;

    @SerializedName("reviews_url")
    private URI reviewsUrl;

    private List<String> unlockables;

    public Course() {
        this(null);
    }

    public Course(String name) {
        this.name = name;
        this.exercises = new ArrayList<>();
        this.skills = new ArrayList<>();
        this.unlockables = new ArrayList<>();
        this.spywareUrls = new ArrayList<>();
    }

    public List<URI> getSpywareUrls() {
        return spywareUrls;
    }

    public void setSpywareUrls(List<URI> spywareUrls) {
        this.spywareUrls = spywareUrls;
    }

    public List<Exercise> getExercises() {
        return exercises;
    }

    public void setExercises(List<Exercise> exercises) {
        this.exercises = exercises;
    }

    public int getWeekCount() {
        return weekCount;
    }

    public List<Skill> getSkills() {
        return skills;
    }

    public void setSkills(List<Skill> skills) {
        this.skills = skills;
    }

    public List<Skill> getSkillsByWeek(int week) {
        List<Skill> returnSkills = new ArrayList<>();
        for (Skill skill : skills) {
            if (skill.getWeek() == week) {
                returnSkills.add(skill);
            }
        }
        return returnSkills;
    }

    public void generateWeeks() {
        for (Exercise ex : exercises) {
            ex.generateWeek();
        }
    }

    public List<Exercise> getExercisesByWeek(int week) {
        List<Exercise> exercisesWithWeek = new ArrayList<>();
        for (Exercise ex : exercises) {
            if (ex.getWeek() == week) {
                exercisesWithWeek.add(ex);
            }
        }
        return exercisesWithWeek;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public URI getDetailsUrl() {
        return detailsUrl;
    }

    public void setDetailsUrl(URI detailsUrl) {
        this.detailsUrl = detailsUrl;
    }

    private boolean exercisesLoaded;

    public URI getUnlockUrl() {
        return unlockUrl;
    }

    public void setUnlockUrl(URI unlockUrl) {
        this.unlockUrl = unlockUrl;
    }

    public URI getCometUrl() {
        return cometUrl;
    }

    public void setCometUrl(URI cometUrl) {
        this.cometUrl = cometUrl;
    }

    public boolean isExercisesLoaded() {
        return exercisesLoaded;
    }

    public void setExercisesLoaded(boolean exercisesLoaded) {
        this.exercisesLoaded = exercisesLoaded;
    }

    public List<String> getUnlockables() {
        return unlockables;
    }

    public void setUnlockables(List<String> unlockables) {
        this.unlockables = unlockables;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + this.id;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Course other = (Course) obj;
        return this.id == other.id;
    }

    @Override
    public String toString() {
        /*
           TODO: this cannot return anything else until PreferencesPanel is fixed to...
           not use toString() to present Course objects
        */
        return name;
    }

    public void setWeekCount(int weeks) {
        this.weekCount = weeks;
    }

}
