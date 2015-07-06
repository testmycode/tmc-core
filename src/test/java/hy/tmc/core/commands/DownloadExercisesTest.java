package hy.tmc.core.commands;


import com.google.common.base.Optional;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import hy.tmc.core.communication.ExerciseDownloader;
import hy.tmc.core.communication.TmcJsonParser;
import hy.tmc.core.testhelpers.ClientTmcSettings;
import hy.tmc.core.domain.Course;
import hy.tmc.core.domain.Exercise;
import hy.tmc.core.exceptions.TmcCoreException;
import hy.tmc.core.testhelpers.builders.ExerciseBuilder;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import org.mockito.Mockito;

public class DownloadExercisesTest {

    private File cache;
    private ClientTmcSettings settings;
    private TmcJsonParser parser;
    
    @Before
    public void setup() throws IOException {
        settings = new ClientTmcSettings();
        settings.setUsername("Bossman");
        settings.setPassword("Samu");
        cache = Paths.get("src", "test", "resources", "downloadtest.cache").toFile();
        cache.createNewFile();
        parser = Mockito.mock(TmcJsonParser.class);
    }
    
    @After
    public void tearDown() {
        cache.delete();
    }
    
    /**
     * Check that data checking success.
     */
    @Test
    public void testCheckDataSuccess() throws TmcCoreException {
        settings.setUsername("Bossman");
        settings.setPassword("Samu");
        DownloadExercises de = new DownloadExercises(settings);
        de.setParameter("path", "/home/tmccli/uolevipuistossa");
        de.setParameter("courseID", "21");
        de.checkData();
    }

    /**
     * Check that if user didn't give correct data, data checking fails.
     */
    @Test(expected = TmcCoreException.class)
    public void testCheckDataFail() throws TmcCoreException {
        DownloadExercises de = new DownloadExercises(settings);
        de.checkData();
    }

    /**
     * User gives course id that isn't a number and will be informed about it.
     */
    @Test(expected = TmcCoreException.class)
    public void courseIdNotANumber() throws TmcCoreException {
        DownloadExercises de = new DownloadExercises(settings);
        de.setParameter("path", "/home/tmccli/uolevipuistossa");
        de.setParameter("courseID", "not a number");
        de.checkData();
    }
    
    @Test
    public void writesChecksumsToFileIfCacheFileIsGiven() throws IOException, TmcCoreException {
        ExerciseDownloader downloader = Mockito.mock(ExerciseDownloader.class);
        Mockito.when(downloader.createCourseFolder(anyString(), anyString()))
                .thenReturn("");
        Mockito.when(downloader.handleSingleExercise(
                any(Exercise.class), anyInt(), anyInt(), anyString())
        ).thenReturn("");
        
        Course course = new Course();
        course.setExercises(new ExerciseBuilder()
                .withExercise("kissa", 2, "eujwuc")
                .withExercise("asdf", 793, "alnwnec")
                .withExercise("ankka", 88, "abcdefg")
                .build());
        
        parser = Mockito.mock(TmcJsonParser.class);
        Mockito.when(parser.getCourse(anyInt())).thenReturn(Optional.of(course));
        
        DownloadExercises dl = new DownloadExercises(downloader, "", "8", cache, settings, parser);
        dl.call();
        String json = FileUtils.readFileToString(cache);
        Gson gson = new Gson();
        Map<Integer,String> checksums;
        Type typeOfHashMap = new TypeToken<Map<Integer, String>>() { }.getType();
        checksums = gson.fromJson(json, typeOfHashMap);
        
        assertNotNull(checksums);
        assertTrue(checksums.containsKey(2));
        assertTrue(checksums.containsKey(793));
        assertTrue(checksums.containsKey(88));
        assertEquals("eujwuc", checksums.get(2));
        assertEquals("alnwnec", checksums.get(793));
        assertEquals("abcdefg", checksums.get(88));

    }
    
    @Test
    public void keepsOldChecksumsInTheCache() throws IOException, TmcCoreException {
        try (FileWriter writer = new FileWriter(cache)) {
            writer.write("{\"33\":\"qwerty\",\"94\":\"aijw9\"}");
        }
        
        ExerciseDownloader mock = Mockito.mock(ExerciseDownloader.class);
        Mockito.when(mock.createCourseFolder(anyString(), anyString()))
                .thenReturn("");
        Mockito.when(mock.handleSingleExercise(
                any(Exercise.class), anyInt(), anyInt(), anyString())
        ).thenReturn("");
        
        Course course = new Course();
        course.setExercises(new ExerciseBuilder()
                .withExercise("kissa", 2, "eujwuc")
                .withExercise("asdf", 793, "alnwnec")
                .withExercise("ankka", 88, "abcdefg")
                .build());
        
        parser = Mockito.mock(TmcJsonParser.class);
        Mockito.when(parser.getCourse(anyInt())).thenReturn(Optional.of(course));
        
        DownloadExercises dl = new DownloadExercises(mock, "", "8", cache, settings, parser);
        dl.call();
        String json = FileUtils.readFileToString(cache);
        Type typeOfHashMap = new TypeToken<Map<Integer, String>>() { }.getType();
        HashMap<Integer, String> checksums = new Gson().fromJson(json, typeOfHashMap);
        
        assertNotNull(checksums);
        assertTrue(checksums.containsKey(2));
        assertTrue(checksums.containsKey(33));
        assertTrue(checksums.containsKey(88));
        assertTrue(checksums.containsKey(94));
        assertEquals("eujwuc", checksums.get(2));
        assertEquals("qwerty", checksums.get(33));
        assertEquals("abcdefg", checksums.get(88));
        assertEquals("aijw9", checksums.get(94));
    }
}
