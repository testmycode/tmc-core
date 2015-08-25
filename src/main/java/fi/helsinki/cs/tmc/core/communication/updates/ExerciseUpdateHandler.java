package fi.helsinki.cs.tmc.core.communication.updates;

import fi.helsinki.cs.tmc.core.cache.ExerciseChecksumCache;
import fi.helsinki.cs.tmc.core.communication.TmcApi;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.ExerciseIdentifier;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ExerciseUpdateHandler extends UpdateHandler<Exercise> {

    private ExerciseChecksumCache cache;
    private ConcurrentMap<ExerciseIdentifier, String> checksumCache;

    public ExerciseUpdateHandler(ExerciseChecksumCache cache, TmcApi tmcApi)
            throws TmcCoreException {
        super(tmcApi);
        checksumCache = new ConcurrentHashMap<>();
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
        this.checksumCache = cache.read();
        if (exercises == null) {
            return new ArrayList<>();
        }
        return exercises;
    }

    @Override
    protected boolean isNew(Exercise exercise) {
        for (ExerciseIdentifier exerciseIdentifier : checksumCache.keySet()) {
            if (exerciseIdentifier.identifies(exercise)) {
                String cachedChecksum = checksumCache.get(exerciseIdentifier);
                String newChecksum = exercise.getChecksum();

                return !cachedChecksum.equals(newChecksum);
            }
        }

        return true;
    }
}
