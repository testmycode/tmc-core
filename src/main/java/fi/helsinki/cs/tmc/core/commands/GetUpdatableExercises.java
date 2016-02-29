package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.TmcServerCommunicationTaskFactory;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;
import fi.helsinki.cs.tmc.core.holders.TmcSettingsHolder;

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

    public GetUpdatableExercises(ProgressObserver observer) {
        super(observer);
    }


    @Override
    public List<Exercise> call() throws TmcCoreException {
        Optional<Course> currentCourse = TmcSettingsHolder.get().getCurrentCourse();
        if (!currentCourse.isPresent()) {
            logger.warn("Attempted to check for updatable exercises without a current "
                    + "course");
            throw new TmcCoreException("Can not check for exercise updates when no course "
                    + "is selected");
        }

        Callable<Course> fullCourseInfoTask = new TmcServerCommunicationTaskFactory()
                .getFullCourseInfoTask(currentCourse.get());

        List<Exercise> newExercises;
        try {
            newExercises = fullCourseInfoTask.call().getExercises();
        } catch (Exception ex) {
            logger.warn("Failed to fetch exercises from server", ex);
            throw new TmcCoreException("Failed to fetch exercises from server", ex);
        }

        List<Exercise> updatableExercises = new ArrayList<>();
        for (Exercise currentExercise : currentCourse.get().getExercises()) {
            Optional<Exercise> replacementExercise = getReplacementExercise(
                    currentExercise,
                    newExercises);
            if (replacementExercise.isPresent()) {
                updatableExercises.add(replacementExercise.get());
            }
        }

        return updatableExercises;
    }

    private Optional<Exercise> getReplacementExercise(
            Exercise oldExercise,
            List<Exercise> newExercises) {
        for (Exercise newExercise : newExercises) {
            if (oldExercise.isSameExercise(newExercise)
                    && !oldExercise.getChecksum().equals(newExercise.getChecksum())) {
                return Optional.of(newExercise);
            }
        }

        return Optional.absent();
    }
}
