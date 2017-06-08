package fi.helsinki.cs.tmc.core.communication.serialization;

import fi.helsinki.cs.tmc.core.domain.Exercise;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ExerciseListParser {

    private static final Logger logger = LoggerFactory.getLogger(CourseListParser.class);

    public List<Exercise> parseFromJson(String json) {
        if (json == null) {
            throw new NullPointerException("Json string is null");
        }
        if (json.trim().isEmpty()) {
            throw new IllegalArgumentException("Empty input");
        }
        try {
            Gson gson =
                    new GsonBuilder()
                            .registerTypeAdapter(Date.class, new CustomDateDeserializer())
                            .create();

            Exercise[] exercises = gson.fromJson(json, Exercise[].class);
            List<Exercise> exerciseList = new ArrayList<>();
            exerciseList.addAll(Arrays.asList(exercises));
            return exerciseList;
        } catch (RuntimeException ex) {
            logger.warn("Failed to parse exercises info", ex);
            throw new RuntimeException("Failed to parse adaptive exercise list: " + ex.getMessage(), ex);
        }
    }
}
