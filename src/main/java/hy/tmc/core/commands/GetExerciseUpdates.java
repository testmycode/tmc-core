package hy.tmc.core.commands;

import hy.tmc.core.communication.updates.ExerciseUpdateHandler;
import hy.tmc.core.domain.Course;
import hy.tmc.core.domain.Exercise;
import hy.tmc.core.exceptions.TmcCoreException;
import java.io.IOException;
import java.util.List;


public class GetExerciseUpdates extends Command<List<Exercise>> {
    
    private final Course course;
    private final ExerciseUpdateHandler handler;
    
    public GetExerciseUpdates(Course course, ExerciseUpdateHandler handler) {
        this.course = course;
        this.handler = handler;
    }
    
    @Override
    public void checkData() throws TmcCoreException, IOException {
        if (handler == null) {
            throw new TmcCoreException("updatehandler must be given");
        }
    }

    @Override
    public List<Exercise> call() throws Exception {
        return handler.getNewObjects(course);
    }

}
