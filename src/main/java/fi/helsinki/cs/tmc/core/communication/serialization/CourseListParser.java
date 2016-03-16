package fi.helsinki.cs.tmc.core.communication.serialization;

import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CourseListParser {
    
    private static class CourseListContainer {
        public int apiVersion;
        public Course[] courses;
    }

    private static final Logger logger = LoggerFactory.getLogger(CourseListParser.class);
    
    public List<Course> parseFromJson(String json) {
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
            
            Course[] courses = gson.fromJson(json, CourseListContainer.class).courses;

            List<Course> courseList = new ArrayList<>();
            for (Course course : courses) {
                courseList.add(course);
                course.setExercisesLoaded(false);
                for (Exercise ex : course.getExercises()) {
                    ex.setCourseName(course.getName());
                }
            }

            return courseList;
        } catch (RuntimeException ex) {
            logger.warn("Failed to parse course info", ex);
            throw new RuntimeException("Failed to parse course list: " + ex.getMessage(), ex);
        }
    }
}
