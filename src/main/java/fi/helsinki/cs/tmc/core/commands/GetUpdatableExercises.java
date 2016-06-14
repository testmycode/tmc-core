package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.TmcServerCommunicationTaskFactory;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

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

    private final Course course;

    public GetUpdatableExercises(ProgressObserver observer, Course course) {
        super(observer);
        Preconditions.checkNotNull(course);
        this.course = course;
    }

    @VisibleForTesting
    GetUpdatableExercises(
            ProgressObserver observer,
            TmcServerCommunicationTaskFactory tmcServerCommunicationTaskFactory,
            Course course) {
        super(observer, tmcServerCommunicationTaskFactory);
        this.course = course;
    }

    // TODO(jamo,loezi): what about new exercises?
    @Override
    public List<Exercise> call() throws TmcCoreException {
        Course updatedCourse;
        try {
            updatedCourse = new GetCourseDetails(observer, course, tmcServerCommunicationTaskFactory).call();
        } catch (Exception ex) {
            logger.warn("Failed to fetch exercises from server", ex);
            throw new TmcCoreException("Failed to fetch exercises from server", ex);
        }

        List<Exercise> newExercises = updatedCourse.getExercises();

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

    // Matches exercise with same name and course name. Returns it if checksums differ.
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
