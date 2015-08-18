package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.updates.ExerciseUpdateHandler;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;

import java.util.List;

public class GetExerciseUpdates extends Command<List<Exercise>> {

    private final Course course;
    private final ExerciseUpdateHandler handler;

    public GetExerciseUpdates(Course course, ExerciseUpdateHandler handler, TmcSettings settings) {
        super(settings);
        this.course = course;
        this.handler = handler;
    }

    @Override
    public List<Exercise> call() throws Exception {
        return handler.getNewObjects(course);
    }
}
