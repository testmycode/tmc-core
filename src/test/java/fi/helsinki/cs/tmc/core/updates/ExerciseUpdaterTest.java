
package fi.helsinki.cs.tmc.core.updates;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;

import fi.helsinki.cs.tmc.core.communication.HttpResult;
import fi.helsinki.cs.tmc.core.communication.TmcJsonParser;
import fi.helsinki.cs.tmc.core.communication.UrlCommunicator;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;
import fi.helsinki.cs.tmc.core.testhelpers.ExampleJson;
import fi.helsinki.cs.tmc.core.testhelpers.builders.ExerciseBuilder;
import fi.helsinki.cs.tmc.core.communication.updates.ExerciseUpdateHandler;
import fi.helsinki.cs.tmc.core.domain.Exercise;

import com.google.gson.Gson;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExerciseUpdaterTest {

    private File cacheFile;
    private ExerciseBuilder builder;
    private UrlCommunicator urlCommunicator;

    @Before
    public void setUp() throws IOException, TmcCoreException {
        cacheFile = Paths.get("src", "test", "resources", "exercisetest.cache").toFile();
        cacheFile.createNewFile();

        builder = new ExerciseBuilder();
        urlCommunicator = Mockito.mock(UrlCommunicator.class);
        Mockito.when(urlCommunicator.makeGetRequest(anyString(), any(String[].class)))
                .thenReturn(new HttpResult(ExampleJson.courseExample, 200, true));
    }

    @After
    public void tearDown() {
        cacheFile.delete();
    }

    @Test
    public void getsCorrectExercisesFromServer() throws Exception {
        int numberOfExercises = 153;
        TmcJsonParser tmcJsonParser = Mockito.mock(TmcJsonParser.class);
        Mockito.when(tmcJsonParser.getExercisesFromServer(any(Course.class)))
                .thenReturn(makeExerciseList(numberOfExercises));
        ExerciseUpdateHandler handler = new ExerciseUpdateHandler(cacheFile, tmcJsonParser);
        List<Exercise> exercises = handler.fetchFromServer(new Course());
        assertEquals(numberOfExercises, exercises.size());
    }

    @Test
    public void getsCorrectExercises() throws IOException, Exception {
        Map<String, Map<String, String>> checksums = new HashMap<>();
        checksums.put("test-course", new HashMap());
        checksums.get("test-course").put("old", "abcdefg");
        try (FileWriter writer = new FileWriter(this.cacheFile)) {
            writer.write(new Gson().toJson(checksums));
        }

        TmcJsonParser tmcJsonParser = mockTmcJsonParser();
        ExerciseUpdateHandler handler = new ExerciseUpdateHandler(cacheFile, tmcJsonParser);

        List<Exercise> exercises = handler.getNewObjects(new Course());

        assertEquals(3, exercises.size());
        assertTrue(listHasExerciseWithName(exercises, "new"));
        assertTrue(listHasExerciseWithName(exercises, "changed"));
        assertFalse(listHasExerciseWithName(exercises, "old"));
    }

    @Test
    public void getsCorrectExercisesWithEmptyCache() throws IOException, Exception {

        TmcJsonParser tmcJsonParser = mockTmcJsonParser();
        ExerciseUpdateHandler handler = new ExerciseUpdateHandler(cacheFile, tmcJsonParser);

        List<Exercise> exercises = handler.getNewObjects(new Course());
        assertEquals(4, exercises.size());
        assertTrue(listHasExerciseWithName(exercises, "old"));
        assertTrue(listHasExerciseWithName(exercises, "duck"));
    }

    private TmcJsonParser mockTmcJsonParser() throws IOException {
        TmcJsonParser tmcJsonParser = Mockito.mock(TmcJsonParser.class);
        List<Exercise> serverExercises =
                builder
                        .withExercise("old", 5, "abcdefg", "test-course")
                        .withExercise("changed", 7, "oeoeoo", "test-course")
                        .withExercise("new", 8, "woksirjd", "test-course")
                        .withExercise("duck", 9, "asdfsdf", "test-course")
                        .build();
        Mockito.when(tmcJsonParser.getExercisesFromServer(any(Course.class)))
                .thenReturn(serverExercises);
        return tmcJsonParser;
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
