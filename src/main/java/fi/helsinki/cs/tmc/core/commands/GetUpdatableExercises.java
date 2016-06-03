package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.TmcServerCommunicationTaskFactory;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;
import fi.helsinki.cs.tmc.core.holders.TmcSettingsHolder;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * A {@link Command} for retrieving exercise updates from TMC server.
 */
public class GetUpdatableExercises extends Command<List<Exercise>> {

    private static final Logger logger = LoggerFactory.getLogger(GetUpdatableExercises.class);
    private Course course;

    public GetUpdatableExercises(ProgressObserver observer) {
        super(observer);
    }

    public GetUpdatableExercises(ProgressObserver observer, Course course) {
        super(observer);
        course = course;
    }

    @VisibleForTesting
    GetUpdatableExercises(
            ProgressObserver observer,
            TmcServerCommunicationTaskFactory tmcServerCommunicationTaskFactory) {
        super(observer, tmcServerCommunicationTaskFactory);
    }

    // TODO(jamo,loezi): what about new exercises?
    @Override
    public List<Exercise> call() throws TmcCoreException {
        if (course == null) {
            Optional<Course> currentCourse = TmcSettingsHolder.get().getCurrentCourse();

            if (!currentCourse.isPresent()) {
                logger.warn("Attempted to check for updatable exercises without a current " + "course");
                throw new TmcCoreException(
                        "Can not check for exercise updates when no course " + "is selected");
            }
            course = currentCourse.get();
        }

        Callable<Course> fullCourseInfoTask =
                tmcServerCommunicationTaskFactory.getFullCourseInfoTask(course);

        List<Exercise> newExercises;
        try {
            // So we won't update anyting or the current course object?!
            newExercises = fullCourseInfoTask.call().getExercises();
        } catch (Exception ex) {
            logger.warn("Failed to fetch exercises from server", ex);
            throw new TmcCoreException("Failed to fetch exercises from server", ex);
        }

        List<Exercise> updatableExercises = new ArrayList<>();
        for (Exercise currentExercise : course.getExercises()) {
            Optional<Exercise> replacementExercise =
                    getReplacementExercise(currentExercise, newExercises);
            if (replacementExercise.isPresent()) {
                updatableExercises.add(replacementExercise.get());
            }
        }

        return updatableExercises;
    }

    // Maches exercise with same name and course name. Returns it if checksums differ
    private Optional<Exercise> getReplacementExercise(
            Exercise oldExercise, List<Exercise> newExercises) {
        for (Exercise newExercise : newExercises) {
            if (oldExercise.isSameExercise(newExercise)
                    && !oldExercise.getChecksum().equals(newExercise.getChecksum())) {
                return Optional.of(newExercise);
            }
        }

        return Optional.absent();
    }
}
