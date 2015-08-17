package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.updates.ExerciseUpdateHandler;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;

import java.util.List;

/**
 * A {@link Command} for retrieving exercise updates from TMC server.
 */
public class GetExerciseUpdates extends Command<List<Exercise>> {

    private final Course course;
    private final ExerciseUpdateHandler handler;

    /**
     * Constructs a new get exercise updates command that fetches exercise updates for
     * {@code course} using {@code handler}.
     */
    public GetExerciseUpdates(Course course, ExerciseUpdateHandler handler) {
        this.course = course;
        this.handler = handler;
    }

    /**
     * Entry point for launching this command.
     */
    @Override
    public List<Exercise> call() throws TmcCoreException {
        try {
            return handler.getNewObjects(course);
        } catch (Exception ex) {
            throw new TmcCoreException("Failed to update exercises", ex);
        }
    }
}
