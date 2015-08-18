package fi.helsinki.cs.tmc.core.communication.updates;

import fi.helsinki.cs.tmc.core.cache.ExerciseChecksumCache;
import fi.helsinki.cs.tmc.core.communication.TmcApi;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.io.FileUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExerciseUpdateHandler extends UpdateHandler<Exercise> {

    private ExerciseChecksumCache cache;
    private Map<String, Map<String, String>> exerciseChecksums;

    public ExerciseUpdateHandler(ExerciseChecksumCache cache, TmcApi tmcApi) throws TmcCoreException {
        super(tmcApi);
        exerciseChecksums = new HashMap<>();
        if (cache == null) {
            String errorMessage = "ExerciseUpdateHandler requires non-null cacheFile to function";
            throw new TmcCoreException(errorMessage);
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
