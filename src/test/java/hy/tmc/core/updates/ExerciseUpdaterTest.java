
package hy.tmc.core.updates;

import com.google.gson.Gson;
import hy.tmc.core.communication.HttpResult;
import hy.tmc.core.communication.TmcJsonParser;
import hy.tmc.core.communication.UrlCommunicator;
import hy.tmc.core.communication.updates.ExerciseUpdateHandler;
import hy.tmc.core.domain.Course;
import hy.tmc.core.domain.Exercise;
import hy.tmc.core.exceptions.TmcCoreException;
import hy.tmc.core.testhelpers.ClientTmcSettings;
import hy.tmc.core.testhelpers.ExampleJson;
import hy.tmc.core.testhelpers.builders.ExerciseBuilder;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import org.powermock.api.mockito.PowerMockito;
import static org.powermock.api.mockito.PowerMockito.when;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({UrlCommunicator.class, TmcJsonParser.class})
public class ExerciseUpdaterTest {
    
    private File cacheFile;
    private ExerciseUpdateHandler handler;
    private ExerciseBuilder builder;
   
    
    @Before
    public void setUp() throws IOException, TmcCoreException {
        cacheFile = Paths.get("src", "test", "resources", "exercisetest.cache").toFile();
        cacheFile.createNewFile();
        handler = new ExerciseUpdateHandler(cacheFile, new ClientTmcSettings());
        builder = new ExerciseBuilder();
        PowerMockito.mockStatic(UrlCommunicator.class);
        when(UrlCommunicator.makeGetRequest(anyString(), any(String[].class)))
                .thenReturn(new HttpResult(ExampleJson.courseExample, 200, true));
    }
    
    @After
    public void tearDown() {
        cacheFile.delete();
    }


    @Test
    public void getsCorrectExercisesFromServer() throws IOException {
        List<Exercise> exercises = this.handler.fetchFromServer(new Course());
        assertEquals(153, exercises.size());
    }
    
    @Test
    public void getsCorrectExercises() throws IOException, Exception {
        Map<Integer, String> checksums = new HashMap<>();
        checksums.put(5, "abcdefg");
        checksums.put(7, "aleialc");
        try (FileWriter writer = new FileWriter(this.cacheFile)) {
            writer.write(new Gson().toJson(checksums));
        }
        
        PowerMockito.mockStatic(TmcJsonParser.class);
        List<Exercise> serverExercises = builder.withExercise("old", 5, "abcdefg")
                                .withExercise("changed", 7, "oeoeoo")
                                .withExercise("new", 8, "woksirjd")
                                .withExercise("duck", 9, "asdfsdf")
                                .build();
        
        when(TmcJsonParser.getExercisesFromServer(any(Course.class)))
                .thenReturn(serverExercises);
        
        List<Exercise> exercises = handler.getNewObjects(new Course());
        
        assertEquals(3, exercises.size());
        assertTrue(listHasExerciseWithName(exercises, "new"));
        assertTrue(listHasExerciseWithName(exercises, "changed"));
        assertFalse(listHasExerciseWithName(exercises, "old"));
    }
    
    @Test
    public void getsCorrectExercisesWithEmptyCache() throws IOException, Exception {
        PowerMockito.mockStatic(TmcJsonParser.class);
        List<Exercise> serverExercises = builder.withExercise("a", 5, "abcdefg")
                                .withExercise("b", 7, "oe14oo")
                                .withExercise("c", 8, "woksirjd")
                                .withExercise("d", 9, "asdf@1df")
                                .build();
        
        when(TmcJsonParser.getExercisesFromServer(any(Course.class)))
                .thenReturn(serverExercises);
        
        List<Exercise> exercises = handler.getNewObjects(new Course());
        
        assertEquals(4, exercises.size());
        assertTrue(listHasExerciseWithName(exercises, "b"));
        assertTrue(listHasExerciseWithName(exercises, "c"));
    }
    
    
    
    private boolean listHasExerciseWithName(List<Exercise> list, String name) {
        for (Exercise exercise : list) {
            if (exercise.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
   
}
