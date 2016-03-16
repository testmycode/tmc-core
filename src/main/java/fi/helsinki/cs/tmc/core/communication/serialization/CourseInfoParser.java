package fi.helsinki.cs.tmc.core.communication.serialization;

import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class CourseInfoParser {

    private static class CourseInfoContainer {
        public int apiVersion;
        public Course course;
    }

    private static final Logger logger = LoggerFactory.getLogger(CourseInfoParser.class);

    public Course parseFromJson(String json) {
        if (json == null) {
            throw new NullPointerException("Json string is null");
        }
        if (json.trim().isEmpty()) {
            throw new IllegalArgumentException("Empty input");
        }
        try {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Date.class, new CustomDateDeserializer())
                    .create();

            Course course = gson.fromJson(json, CourseInfoContainer.class).course;

            course.setExercisesLoaded(true);
            
            for (Exercise ex : course.getExercises()) {
                ex.setCourseName(course.getName());
            }

            return course;
        } catch (RuntimeException ex) {
            logger.warn("Failed to parse course info", ex);
            throw new RuntimeException("Failed to parse course info: " + ex.getMessage(), ex);
        }
    }
}
