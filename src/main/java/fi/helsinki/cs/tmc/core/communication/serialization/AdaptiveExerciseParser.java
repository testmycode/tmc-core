/*
 * Author: Ohtu project summer devs 2017
 */

package fi.helsinki.cs.tmc.core.communication.serialization;

import fi.helsinki.cs.tmc.core.domain.Exercise;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;


public class AdaptiveExerciseParser {
    
    private static final Logger logger = LoggerFactory.getLogger(AdaptiveExerciseParser.class);

    public Exercise parseFromJson(String json) {
        if (json == null) {
            throw new NullPointerException("Json string is null");
        }
        if (json.trim().isEmpty()) {
            throw new IllegalArgumentException("Empty input");
        }
        try {
            JSONObject obj = new JSONObject(json);
            if (obj.getBoolean("available")) {                
                Exercise exercise = new Exercise();
                // localhost, where is Skillifier hosted?
                exercise.setDownloadUrl(URI.create("http://ohtu-skillifier.herokuapp.com"
                           + obj.getString("zip_url")));
                return exercise;
            }
            return null;
        } catch (RuntimeException ex) {
            logger.warn("Failed to parse an adaptive course from URL", ex);
            throw new RuntimeException("Failed to parse an adaptive course from URL: " 
                    + ex.getMessage(), ex);
        }    
    }
    
}
