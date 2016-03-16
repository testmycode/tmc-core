package fi.helsinki.cs.tmc.core.persistance;

import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.ExerciseKey;

import java.util.List;

/**
 * Created by ljleppan on 16.3.2016.
 */
public interface TmcState {
    void setAvailableCourses(List<Course> availableCourses);

    Course getCurrentCourse();

    String getCurrentCourseName();

    void setCurrentCourseName(String currentCourseName);

    void putDetailedCourse(Course course);

    Exercise getExerciseByKey(ExerciseKey key);

    List<Exercise> getCurrentCourseExercises();

    Course getCourseByName(String name);

    boolean isUnlockable(Exercise ex);

    List<Exercise> getCurrentCourseUnlockableExercises();

    String getDownloadedExerciseChecksum(ExerciseKey ex);

    void exerciseDownloaded(Exercise ex);
}
