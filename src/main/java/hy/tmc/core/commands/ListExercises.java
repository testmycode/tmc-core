package hy.tmc.core.commands;

import com.google.common.base.Optional;
import hy.tmc.core.communication.ExerciseLister;
import hy.tmc.core.communication.TmcJsonParser;
import hy.tmc.core.configuration.TmcSettings;
import hy.tmc.core.domain.Course;
import hy.tmc.core.domain.Exercise;

import hy.tmc.core.exceptions.TmcCoreException;
import java.io.IOException;

import java.util.List;

public class ListExercises extends Command<List<Exercise>> {

    private ExerciseLister lister;
    private Course current;

    /**
     * Default constructor.
     */
    public ListExercises(TmcSettings settings) {
        super(settings);
        this.lister = new ExerciseLister(new TmcJsonParser(settings));
    }

    /**
     * For dependency injection for tests.
     *
     * @param lister mocked lister object.
     */
    public ListExercises(ExerciseLister lister, TmcSettings settings) {
        this(settings);
        this.lister = lister;
    }

    public ListExercises(String path, TmcSettings settings) {
        this(settings);
        this.setParameter("path", path);
    }

    /**
     * Check the path and settings..
     *
     * @throws TmcCoreException if some data not specified
     */
    @Override
    public void checkData() throws TmcCoreException, IOException {
        if (!data.containsKey("path")) {
            throw new TmcCoreException("Path not recieved");
        }
        if (!settings.userDataExists()) {
            throw new TmcCoreException("Please authorize first.");
        }
        Course currentCourse = settings.getCurrentCourse();
        if (currentCourse != null) {
            this.current = currentCourse;
        } else {
            throw new TmcCoreException("A course must be selected.");
        }
    }

    @Override
    public List<Exercise> call() throws TmcCoreException, IOException {
        checkData();
        List<Exercise> exercises = lister.listExercises(data.get("path"));
        return exercises;
    }
}
