package fi.helsinki.cs.tmc.core.updates;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fi.helsinki.cs.tmc.core.cache.ExerciseChecksumCache;
import fi.helsinki.cs.tmc.core.communication.HttpResult;
import fi.helsinki.cs.tmc.core.communication.TmcApi;
import fi.helsinki.cs.tmc.core.communication.UrlCommunicator;
import fi.helsinki.cs.tmc.core.communication.updates.ExerciseUpdateHandler;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;
import fi.helsinki.cs.tmc.core.testhelpers.ExampleJson;
import fi.helsinki.cs.tmc.core.testhelpers.builders.ExerciseBuilder;

import com.google.gson.Gson;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExerciseUpdaterTest {

    private ExerciseChecksumCache cache;
    private ExerciseBuilder builder;
    private UrlCommunicator urlCommunicator;

    @Before
    public void setUp() throws IOException, TmcCoreException {
        cache = mock(ExerciseChecksumCache.class);

        builder = new ExerciseBuilder();
        urlCommunicator = mock(UrlCommunicator.class);
        when(urlCommunicator.makeGetRequest(anyString(), any(String.class)))
                .thenReturn(new HttpResult(ExampleJson.courseExample, 200, true));
    }

    @Test
    public void getsCorrectExercisesFromServer() throws Exception {
        int numberOfExercises = 153;
        TmcApi tmcApi = mock(TmcApi.class);
        when(tmcApi.getExercisesFromServer(any(Course.class)))
                .thenReturn(makeExerciseList(numberOfExercises));
        ExerciseUpdateHandler handler = new ExerciseUpdateHandler(cache, tmcApi);
        List<Exercise> exercises = handler.fetchFromServer(new Course());
        assertEquals(numberOfExercises, exercises.size());
    }

    @Test
    public void getsCorrectExercises() throws IOException, Exception {
        Map<String, Map<String, String>> checksums = new HashMap<>();
        checksums.put("test-course", new HashMap<String, String>());
        checksums.get("test-course").put("old", "abcdefg");
        when(cache.read()).thenReturn(checksums);

        TmcApi tmcApi = mockTmcApi();
        ExerciseUpdateHandler handler = new ExerciseUpdateHandler(cache, tmcApi);

        List<Exercise> exercises = handler.getNewObjects(new Course());

        assertEquals(3, exercises.size());
        assertTrue(listHasExerciseWithName(exercises, "new"));
        assertTrue(listHasExerciseWithName(exercises, "changed"));
        assertFalse(listHasExerciseWithName(exercises, "old"));
    }

    @Test
    public void getsCorrectExercisesWithEmptyCache() throws IOException, Exception {

        TmcApi tmcApi = mockTmcApi();
        ExerciseUpdateHandler handler = new ExerciseUpdateHandler(cache, tmcApi);

        List<Exercise> exercises = handler.getNewObjects(new Course());
        assertEquals(4, exercises.size());
        assertTrue(listHasExerciseWithName(exercises, "old"));
        assertTrue(listHasExerciseWithName(exercises, "duck"));
    }

    private TmcApi mockTmcApi() throws IOException, URISyntaxException {
        TmcApi tmcApi = mock(TmcApi.class);
        List<Exercise> serverExercises =
                builder
                        .withExercise("old", 5, "abcdefg", "test-course")
                        .withExercise("changed", 7, "oeoeoo", "test-course")
                        .withExercise("new", 8, "woksirjd", "test-course")
                        .withExercise("duck", 9, "asdfsdf", "test-course")
                        .build();
        when(tmcApi.getExercisesFromServer(any(Course.class))).thenReturn(serverExercises);
        return tmcApi;
    }

    private boolean listHasExerciseWithName(List<Exercise> list, String name) {
        for (Exercise exercise : list) {
            if (exercise.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    private List<Exercise> makeExerciseList(int exercises) {
        List<Exercise> list = new ArrayList<>();
        for (int i = 0; i < exercises; i++) {
            list.add(new Exercise());
        }
        return list;
    }
}
