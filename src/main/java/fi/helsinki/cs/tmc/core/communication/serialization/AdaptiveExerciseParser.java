/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.helsinki.cs.tmc.core.communication.serialization;

import fi.helsinki.cs.tmc.core.domain.Exercise;

import java.net.URI;
import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author sakuolin
 */
public class AdaptiveExerciseParser {
    
    private static final Logger logger = LoggerFactory.getLogger(AdaptiveExerciseParser.class);

    public Exercise parseFromJson(String json) {
        if (json == null) {
            throw new NullPointerException("Json string is null");
        }
        if (json.trim().isEmpty()) {
            throw new IllegalArgumentException("Empty input");
        }
        try {;
            JSONObject obj = new JSONObject(json);
            // Check status
            if (obj.getBoolean("available")) {
                String zip_url = obj.getString("zip_url");
                
                if (true){
                    Exercise ex = new Exercise();
                    ex.setDownloadUrl(URI.create(zip_url));
                    return ex;
                }else{
                    // Käytä GSonia parseemaan kuin Course
                    Gson gson =
                            new GsonBuilder()
                                    .registerTypeAdapter(Date.class, new CustomDateDeserializer())
                                    .create();
                    Exercise exercise = gson.fromJson(json, Exercise.class);
                    // vanha return
                    // tyhjä kommentti koska branch llälä
                    // posdjgpofpgdfojofgjpofgjhfgojhgogf
                    return exercise;
                }
            }
            return null;
        } catch (RuntimeException ex) {
            logger.warn("Failed to parse an adaptive course from URL", ex);
            throw new RuntimeException("Failed to parse an adaptive course from URL: " + ex.getMessage(), ex);
        }    
    }
    
}
