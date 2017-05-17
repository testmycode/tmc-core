/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.helsinki.cs.tmc.core.communication.serialization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import java.util.Date;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author sakuolin
 */
public class AdaptiveExerciseParser {
    
    // TODO: Parse exercise from address
    // TODO: Parse Boolean from JSON
    
    private static final Logger logger = LoggerFactory.getLogger(AdaptiveExerciseParser.class);

    public Boolean parseFromJson(String json) {
        if (json == null) {
            throw new NullPointerException("Json string is null");
        }
        if (json.trim().isEmpty()) {
            throw new IllegalArgumentException("Empty input");
        }
        try {
            JSONObject obj = new JSONObject(json);
            return obj.getBoolean("available");
        } catch (RuntimeException ex) {
            logger.warn("Failed to parse adaptive course availability", ex);
            throw new RuntimeException("Failed to parse adaptive course availability: " + ex.getMessage(), ex);
        }    
    }
    
}
