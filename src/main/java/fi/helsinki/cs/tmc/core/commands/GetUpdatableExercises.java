package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.TmcServerCommunicationTaskFactory;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A {@link Command} for retrieving exercise updates from TMC server.
 */
public class GetUpdatableExercises extends Command<GetUpdatableExercises.UpdateResult> {

    private static final Logger logger = LoggerFactory.getLogger(GetUpdatableExercises.class);

    private final Course course;

    public class UpdateResult {
        private final List<Exercise> created;
        private final List<Exercise> updated;

        private UpdateResult(List<Exercise> created, List<Exercise> updated) {
            this.created = created;
            this.updated = updated;
        }

        public List<Exercise> getCreated() {
            return created;
        }

        public List<Exercise> getUpdated() {
            return updated;
        }
    }

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

    @Override
    public UpdateResult call() throws TmcCoreException {
        Course updatedCourse;
        try {
            updatedCourse = new GetCourseDetails(observer, course, tmcServerCommunicationTaskFactory).call();
        } catch (Exception ex) {
            logger.warn("Failed to fetch exercises from server", ex);
            throw new TmcCoreException("Failed to fetch exercises from server", ex);
        }

        List<Exercise> createExercises = new ArrayList<>();
        List<Exercise> updatedExercises = new ArrayList<>();
        Map<String, Exercise> exerciseMap = new HashMap<>();

        for (Exercise oldExercise : course.getExercises()) {
            exerciseMap.put(oldExercise.getName(), oldExercise);
        }

        for (Exercise newExercise : updatedCourse.getExercises()) {
            Exercise oldExercise = exerciseMap.get(newExercise.getName());
            if (oldExercise == null) {
                createExercises.add(newExercise);
            } else if (!oldExercise.getChecksum().equals(newExercise.getChecksum())) {
                updatedExercises.add(newExercise);
            }
        }

        return new UpdateResult(createExercises, updatedExercises);
    }
}
