package fi.helsinki.cs.tmc.core.communication.updates;

import fi.helsinki.cs.tmc.core.cache.ExerciseChecksumCache;
import fi.helsinki.cs.tmc.core.communication.TmcApi;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Relocate non http elsewhere.
public class ExerciseUpdateHandler extends UpdateHandler<Exercise> {

    private ExerciseChecksumCache cache;
    private Map<String, Map<String, String>> exerciseChecksums;

    public ExerciseUpdateHandler(ExerciseChecksumCache cache, TmcApi tmcApi)
            throws TmcCoreException {
        super(tmcApi);
        exerciseChecksums = new HashMap<>();
        if (cache == null) {
            throw new TmcCoreException(
                    "ExerciseUpdateHandler requires non-null cacheFile to function");
        }
        this.cache = cache;
    }

    @Override
    public List<Exercise> fetchFromServer(Course currentCourse)
            throws TmcCoreException, IOException, URISyntaxException {
        List<Exercise> exercises = tmcApi.getExercisesFromServer(currentCourse);
        this.exerciseChecksums = cache.read();
        if (exercises == null) {
            return new ArrayList<>();
        }
        return exercises;
    }

    @Override
    protected boolean isNew(Exercise exercise) {
        if (exerciseChecksums.containsKey(exercise.getCourseName())
                && exerciseChecksums
                        .get(exercise.getCourseName())
                        .containsKey(exercise.getName())) {
            String earlierChecksum =
                    exerciseChecksums.get(exercise.getCourseName()).get(exercise.getName());
            return !exercise.getChecksum().equals(earlierChecksum);
        }
        return true;
    }
}
