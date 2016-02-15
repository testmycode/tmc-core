package fi.helsinki.cs.tmc.core.testhelpers;

import fi.helsinki.cs.tmc.core.domain.Course;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.Arrays;
import java.util.List;

public class JsonParser {

    /**
     * Reads courses from string.
     */
    public static List<Course> getCoursesFromString(String jsonString) {
        JsonObject jsonObject = new com.google.gson.JsonParser().parse(jsonString).getAsJsonObject();
        Gson mapper = new Gson();
        Course[] courses = mapper.fromJson(jsonObject.getAsJsonArray("courses"), Course[].class);
        return Arrays.asList(courses);
    }

    /**
     * Reads one course from string.
     */
    public static Course getCourseFromString(String jsonString) {
        JsonObject jsonObject = new com.google.gson.JsonParser().parse(jsonString).getAsJsonObject();
        Gson mapper = new Gson();
        return mapper.fromJson(jsonObject.get("course"), Course.class);
    }
}
