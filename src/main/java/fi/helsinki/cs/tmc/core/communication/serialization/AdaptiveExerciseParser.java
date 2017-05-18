/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.helsinki.cs.tmc.core.communication.serialization;

import com.google.gson.*;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import java.net.URI;
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
    
    private static final Logger logger = LoggerFactory.getLogger(AdaptiveExerciseParser.class);

    public Exercise parseFromJson(String json) {
        if (json == null) {
            throw new NullPointerException("Json string is null");
        }
        if (json.trim().isEmpty()) {
            throw new IllegalArgumentException("Empty input");
        }
        try {;
            //Gson gson = new Gson();
            //JsonParser parser = new JsonParser();
            //JsonElement element = parser.parse(json);
            //JsonArray array = element.getAsJsonArray();
            //Boolean availability = gson.fromJson(array.get(0), Boolean.class);
            JSONObject obj = new JSONObject(json);
            boolean availability = obj.getBoolean("available");
            // Check status
            if (availability) {
                String zip_url = obj.getString("zip_url");
                //String zip_url = gson.fromJson(array.get(1), String.class);
                Exercise ex = new Exercise();
                ex.setDownloadUrl(URI.create(zip_url));
                return ex;
                
                // ...
                
                //byte[] zip;
                // Gson
                //gson = new GsonBuilder().create();
                //ercise exercise = gson.fromJson(array.get(1), Exercise.class);
                //return exercise;
                //return zip_url;
            }
            return null;
        } catch (RuntimeException ex) {
            logger.warn("Failed to parse an adaptive course from URL", ex);
            throw new RuntimeException("Failed to parse an adaptive course from URL: " + ex.getMessage(), ex);
        }    
    }
    
}
