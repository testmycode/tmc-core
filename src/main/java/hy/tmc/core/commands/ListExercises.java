package hy.tmc.core.commands;

import com.google.common.base.Optional;
import hy.tmc.core.communication.ExerciseLister;
import hy.tmc.core.configuration.ClientData;
import hy.tmc.core.domain.Course;
import hy.tmc.core.domain.Exercise;

import hy.tmc.core.exceptions.ProtocolException;
import java.io.IOException;

import java.util.List;

public class ListExercises extends Command<List<Exercise>> {

    private ExerciseLister lister;
    private Course current;

    public ListExercises() {
        this(new ExerciseLister());
    }

    /**
     * For dependency injection for tests.
     *
     * @param lister mocked lister object.
     */
    public ListExercises(ExerciseLister lister) {
        this.lister = lister;
    }

    public ListExercises(String path) {
        this(new ExerciseLister());
        this.setParameter("path", path);
    }

    /**
     * Check the path and ClientData.
     *
     * @throws ProtocolException if some data not specified
     */
    @Override
    public void checkData() throws ProtocolException, IOException {
        if (!data.containsKey("path")) {
            throw new ProtocolException("Path not recieved");
        }
        if (!ClientData.userDataExists()) {
            throw new ProtocolException("Please authorize first.");
        }
        Optional<Course> currentCourse = ClientData.getCurrentCourse(data.get("path"));
        if (currentCourse.isPresent()) {
            this.current = currentCourse.get();
        } else {
            throw new ProtocolException("No course resolved from the path.");
        }
    }

    @Override
    public List<Exercise> call() throws ProtocolException, IOException {
        checkData();
        List<Exercise> exercises = lister.listExercises(data.get("path"));
        return exercises;
    }
}
