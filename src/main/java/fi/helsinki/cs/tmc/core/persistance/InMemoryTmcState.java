package fi.helsinki.cs.tmc.core.persistance;

import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.ExerciseKey;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class InMemoryTmcState implements TmcState {

    private Course currentCourse;
    private String currentCourseName;
    private List<Course> availableCourses;
    private Map<ExerciseKey, String> downloadedExerciseChecksums;

    @Override
    public void setAvailableCourses(List<Course> availableCourses) {
        this.availableCourses = availableCourses;
    }

    @Override
    public Course getCurrentCourse() {
        return currentCourse;
    }

    @Override
    public String getCurrentCourseName() {
        return currentCourseName;
    }

    @Override
    public void setCurrentCourseName(String currentCourseName) {
        this.currentCourseName = currentCourseName;
    }

    @Override
    public void putDetailedCourse(Course course) {
        for(int i = 0; i < availableCourses.size(); i++) {
            if (course.getName().equals(availableCourses.get(i).getName())) {
                availableCourses.set(i, course);
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

    @Override
    public List<Exercise> getCurrentCourseUnlockableExercises() {
        List<Exercise> result = new ArrayList<>();
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

    @Override
    public void exerciseDownloaded(Exercise ex) {
        downloadedExerciseChecksums.put(ex.getKey(), ex.getChecksum());
    }
}
