package fi.helsinki.cs.tmc.core.persistance;

import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.ExerciseKey;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TmcState {

    private String currentCourseName;
    private List<Course> availableCourses;
    private Map<ExerciseKey, String> downloadedExerciseChecksums;

    public TmcState() {
        this.availableCourses = new ArrayList<>();
        this.downloadedExerciseChecksums = new HashMap<>();
    }

    public List<Course> getAvailableCourses() {
        return Collections.unmodifiableList(availableCourses);
    }

    public void setAvailableCourses(List<Course> availableCourses) {
        this.availableCourses = availableCourses;
    }

    public Course getCurrentCourse() throws TmcCoreException {
        try {
            return getCourseByName(currentCourseName);
        } catch (TmcCoreException ex) {
            throw new TmcCoreException("Failed to fetch current course", ex);
        }
    }

    public String getCurrentCourseName() {
        return currentCourseName;
    }

    public void setCurrentCourseName(String currentCourseName) {
        this.currentCourseName = currentCourseName;
    }

    public void putDetailedCourse(Course course) {
        for (int i = 0; i < availableCourses.size(); i++) {
            if (course.getName().equals(availableCourses.get(i).getName())) {
                availableCourses.set(i, course);
                break;
            }
        }
    }

    public Exercise getExerciseByKey(ExerciseKey key) throws TmcCoreException {
        for (Exercise ex : getCurrentCourseExercises()) {
            if (key.equals(ex.getKey())) {
                return ex;
            }
        }
        throw new TmcCoreException("No matching exercise found in current course");
    }

    public List<Exercise> getCurrentCourseExercises() throws TmcCoreException {
        Course course = getCurrentCourse();
        if (course != null) {
            return course.getExercises();
        } else {
            return Collections.emptyList();
        }
    }

    public Course getCourseByName(String name) throws TmcCoreException {
        for (Course course : availableCourses) {
            if (course.getName().equals(name)) {
                return course;
            }
        }
        throw new TmcCoreException(
            "Course name " + name + " does not match any available course");
    }

    public boolean isUnlockable(Exercise ex) throws TmcCoreException {
        Course course = getCourseByName(ex.getCourseName());
        return course != null && course.getUnlockables().contains(ex.getName());
    }

    public List<Exercise> getCurrentCourseUnlockableExercises() throws TmcCoreException {
        List<Exercise> result = new ArrayList<>();
        Course course = getCurrentCourse();
        if (course != null) {
            List<URI> unlockables = course.getUnlockables();
            if (unlockables == null) {
                unlockables = Collections.emptyList();
            }
            // TODO: bug? uri vs string
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

    public String getDownloadedExerciseChecksum(ExerciseKey ex) {
        return downloadedExerciseChecksums.get(ex);
    }

    public void exerciseDownloaded(Exercise ex) {
        downloadedExerciseChecksums.put(ex.getKey(), ex.getChecksum());
    }
}
