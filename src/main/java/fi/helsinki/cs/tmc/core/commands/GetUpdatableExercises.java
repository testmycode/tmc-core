package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.TmcServerCommunicationTaskFactory;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;
import fi.helsinki.cs.tmc.core.utilities.ServerErrorHelper;

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

        public List<Exercise> getNewExercises() {
            return created;
        }

        public List<Exercise> getUpdatedExercises() {
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
        logger.info("Looking for updatedable exercises");
        informObserver(0, "Downloading course details from server");
        Course updatedCourse;
        try {
            updatedCourse = new GetCourseDetails(
                    ProgressObserver.NULL_OBSERVER,
                    course,
                    tmcServerCommunicationTaskFactory).call();
        } catch (Exception ex) {
            logger.warn("Failed to fetch exercises from server", ex);
            throw new TmcCoreException("Failed to fetch exercises from server. \n"
                + ServerErrorHelper.getServerExceptionMsg(ex), ex);
        }

        checkInterrupt();
        logger.debug("Parsing results");
        informObserver(0.5, "Parsing response");

        List<Exercise> createExercises = new ArrayList<>();
        List<Exercise> updatedExercises = new ArrayList<>();
        Map<String, Exercise> oldExercises = new HashMap<>();

        for (Exercise oldExercise : course.getExercises()) {
            oldExercises.put(oldExercise.getName(), oldExercise);
        }

        List<Exercise> exercises = updatedCourse.getExercises();
        int totalExercises = exercises.size();
        for (int i = 0; i < totalExercises; i++) {
            Exercise newExercise = exercises.get(i);

            checkInterrupt();
            informObserver(totalExercises + i, totalExercises * 2, "Parsing downloaded data");

            Exercise oldExercise = oldExercises.get(newExercise.getName());
            if (oldExercise == null) {
                createExercises.add(newExercise);
            } else if (!oldExercise.getChecksum().equals(newExercise.getChecksum())) {
                updatedExercises.add(newExercise);
            }
        }

        logger.debug("Parsing done");
        informObserver(1, "Done checking for updatable exercises");

        return new UpdateResult(createExercises, updatedExercises);
    }
}
