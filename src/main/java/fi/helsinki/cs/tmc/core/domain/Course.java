package fi.helsinki.cs.tmc.core.domain;

import com.google.gson.annotations.SerializedName;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Course {

    private int id;
    private String name;
    private String title;
    private String description;

    private List<Exercise> exercises;

    private List<Theme> themes;

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

    public List<Theme> getThemes() {
        return themes;
    }

    /*
    public void setThemes(List<Theme> themes) {
        this.themes = themes;
        for (final Theme theme : themes) {
            //exercises.stream().filter(theme::shouldContain).collect(Collectors.toList());
            List<Exercise> l = new ArrayList<>();
            for (Exercise ex : exercises) {
                if (theme.shouldContain(ex)) {
                    l.add(ex);
                }
            }
            theme.setExercises(l);
        }
    }
    */

    public void generateThemes() {
        themes = new ArrayList<>();
        Map<String, Theme> themeMap = new HashMap<>();
        for (Exercise ex : exercises) {
            addExerciseToTheme(themeMap, ex);
        }
    }

    private void addExerciseToTheme(Map<String, Theme> themeMap, Exercise ex) {
        String themeName = ex.getName().split("-")[0];
        Theme theme = themeMap.get(themeName);
        if (theme == null) {
            theme = new Theme(themeName);
            themeMap.put(themeName, theme);
            themes.add(theme);
        }
        theme.addExercise(ex);
    }

    public List<Exercise> getExercisesByTheme(String themeName) {
        for (Theme theme : themes) {
            if (theme.getName().equals(themeName)) {
                return theme.getExercises();
            }
        }
        return new ArrayList<>();
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

    public void setThemes(List<Theme> themes) {
        this.themes = themes;
    }

    public void addSkillsToThemes(List<Skill> skillsFromSkillifier) {
        for (Skill skill : skillsFromSkillifier) {
            String themeName = skill.getThemeName();
            Theme skillTheme = null;
            for (Theme theme : themes) {
                if (theme.getName().equals(themeName)) {
                    skillTheme = theme;
                    break;
                }
            }
            if (skillTheme == null) {
                themes.add(skillTheme = new Theme(themeName));
            }
            skillTheme.addSkill(skill);
        }
    }
}
