package fi.helsinki.cs.tmc.core.persistance;

import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.ExerciseKey;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiskPersistableTmcState implements TmcState {

    private ConfigFile configFile;
    private List<Course> availableCourses;
    private String currentCourseName;
    private Map<ExerciseKey, String> downloadedExerciseChecksums;

    private DiskPersistableTmcState() {
        this(new ConfigFile("CourseDb.json"));
    }

    public DiskPersistableTmcState(ConfigFile configFile) {
        this.configFile = configFile;
        this.availableCourses = new ArrayList<>();
        this.currentCourseName = null;
        this.downloadedExerciseChecksums = new HashMap<>();
        try {
            loadFromFile();
        } catch (Exception e) {
            //logger.log(Level.WARNING, "Failed to load course database", e);
        }
    }


    List<Course> getAvailableCourses() {
        return Collections.unmodifiableList(availableCourses);
    }

    @Override
    public void setAvailableCourses(List<Course> availableCourses) {
        this.availableCourses = availableCourses;
        save();
    }

    @Override
    public Course getCurrentCourse() {
        return getCourseByName(availableCourses, currentCourseName);
    }

    @Override
    public String getCurrentCourseName() {
        return currentCourseName;
    }

    @Override
    public void setCurrentCourseName(String currentCourseName) {
        this.currentCourseName = currentCourseName;
        save();
    }

    @Override
    public void putDetailedCourse(Course course) {
        for (int i = 0; i < availableCourses.size(); ++i) {
            if (availableCourses.get(i).getName().equals(course.getName())) {
                availableCourses.set(i, course);
                save();
                break;
            }
        }
    }

    @Override
    public Exercise getExerciseByKey(ExerciseKey key) {
        for (Exercise ex : getCurrentCourseExercises()) {
            if (key.equals(ex.getKey())) {
                return ex;
            }
        }
        return null;
    }

    /**
     * Returns the exercises from currently selected course.
     *
     * <p>
     * If no course is currently selected then returns the empty collection.
     */
    @Override
    public List<Exercise> getCurrentCourseExercises() {
        Course course = getCurrentCourse();
        if (course != null) {
            return course.getExercises();
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public Course getCourseByName(String name) {
        for (Course course : availableCourses) {
            if (course.getName().equals(name)) {
                return course;
            }
        }
        return null;
    }

    @Override
    public boolean isUnlockable(Exercise ex) {
        Course course = getCourseByName(ex.getCourseName());
        if (course != null) {
            return course.getUnlockables().contains(ex.getName());
        } else {
            return false;
        }
    }

    /**
     * Returns all exercises from the current course that can be unlocked (and must be unlocked together).
     */
    @Override
    public List<Exercise> getCurrentCourseUnlockableExercises() {
        List<Exercise> result = new ArrayList<Exercise>();
        Course course = getCurrentCourse();
        if (course != null) {
            List<URI> unlockables = course.getUnlockables();
            if (unlockables == null) {
                unlockables = Collections.emptyList();
            }
            for (URI exerciseName : unlockables) {
                for (Exercise ex : course.getExercises()) {
                    if (ex.getName().equals(exerciseName)) {
                        result.add(ex);
                    }
                }
            }
        }
        return result;
    }

    @Override
    public String getDownloadedExerciseChecksum(ExerciseKey ex) {
        return downloadedExerciseChecksums.get(ex);
    }

    /**
     * Informs the course database that the exercise is considered downloaded.
     *
     * <p>
     * Sets the downloaded checksum of the exercise to be the one reported by the server.
     */
    @Override
    public void exerciseDownloaded(Exercise ex) {
        downloadedExerciseChecksums.put(ex.getKey(), ex.getChecksum());
        save();
    }

    //TODO: arrange for downloadedExerciseChecksums.put(..., null) when a project is deleted!

    public void save() {
        try {
            saveToFile();
        } catch (Exception e) {
            //TODO: Log
        }
    }

    private Course getCourseByName(List<Course> courses, String courseName) {
        for (Course course : courses) {
            if (course.getName().equals(courseName)) {
                return course;
            }
        }
        return null;
    }

    private static class StoredStuff {
        public List<Course> availableCourses;
        public String currentCourseName;
        public Map<ExerciseKey, String> downloadedExerciseChecksums;
    }

    private void saveToFile() throws IOException {
        StoredStuff stuff = new StoredStuff();
        stuff.availableCourses = this.availableCourses;
        stuff.currentCourseName = this.currentCourseName;
        stuff.downloadedExerciseChecksums = this.downloadedExerciseChecksums;
        Writer w = configFile.getWriter();
        try {
            getGson().toJson(stuff, w);
        } finally {
            w.close();
        }
    }

    private void loadFromFile() throws IOException {
        if (!configFile.exists()) {
            return;
        }

        Reader reader = configFile.getReader();
        StoredStuff stuff;
        try {
            stuff = getGson().fromJson(reader, StoredStuff.class);
        } finally {
            reader.close();
        }
        if (stuff != null) {
            if (stuff.availableCourses != null) {
                this.availableCourses.clear();
                this.availableCourses.addAll(stuff.availableCourses);
            }

            this.currentCourseName = stuff.currentCourseName;

            if (stuff.downloadedExerciseChecksums != null) {
                this.downloadedExerciseChecksums.clear();
                this.downloadedExerciseChecksums.putAll(stuff.downloadedExerciseChecksums);
            }
        }
    }

    private Gson getGson() {
        return new GsonBuilder()
                .serializeNulls()
                .setPrettyPrinting()
                .registerTypeAdapter(ExerciseKey.class, new ExerciseKey.GsonAdapter())
                .create();
    }

}
