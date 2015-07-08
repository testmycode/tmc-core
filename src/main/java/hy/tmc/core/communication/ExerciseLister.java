package hy.tmc.core.communication;

import com.google.common.base.Optional;
import hy.tmc.core.configuration.TmcSettings;
import hy.tmc.core.domain.Course;
import hy.tmc.core.domain.Exercise;
import hy.tmc.core.exceptions.TmcCoreException;
import hy.tmc.core.zipping.DefaultRootDetector;
import hy.tmc.core.zipping.ProjectRootFinder;
import hy.tmc.core.zipping.RootFinder;
import java.io.IOException;
import java.util.List;

public class ExerciseLister {

    private RootFinder finder;
    private TmcJsonParser tmcJsonParser;

    /**
     * Default Constructor with default root finder.
     */
    public ExerciseLister(TmcJsonParser jsonParser) {
        this(new ProjectRootFinder(jsonParser), jsonParser);
        
    }

    /**
     * Constructor with specific finder.
     *
     * @param finder a RootFinder instance.
     */
    public ExerciseLister(RootFinder finder, TmcJsonParser jsonParser) {
        this.finder = finder;
        tmcJsonParser = jsonParser;
    }

    /**
     * Returns a list of exercises of a current directory in which a course exists.
     *
     * @param path directory path to lookup course from
     * @return String with a list of exercises.
     */
    public List<Exercise> listExercises(String path) throws TmcCoreException, IOException {
        Optional<Course> course = finder.getCurrentCourse(path);

        if (!course.isPresent()) {
            throw new TmcCoreException("No course found");
        }

        List<Exercise> exercises = tmcJsonParser.getExercisesFromServer(course.get());
        if (exercises == null || exercises.isEmpty()) {
            throw new TmcCoreException("No exercises found");
        }

        return exercises;
    }
}
