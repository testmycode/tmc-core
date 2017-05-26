package fi.helsinki.cs.tmc.core.domain;

import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.Test;

public class AdaptiveExerciseTest {
    
    @Test
    public void testParsingFromJson() {
        String json = "{available:true, zip_url:none}";
        Gson gson = new GsonBuilder().create();
        AdaptiveExercise adaptive = gson.fromJson(json, AdaptiveExercise.class);
        assertTrue(adaptive.getAvailable());
    }
    
}
