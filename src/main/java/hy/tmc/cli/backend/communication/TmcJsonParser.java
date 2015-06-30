package hy.tmc.cli.backend.communication;

import com.google.common.base.Optional;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import hy.tmc.cli.configuration.ClientData;
import hy.tmc.cli.configuration.ConfigHandler;
import hy.tmc.cli.domain.Course;
import hy.tmc.cli.domain.Exercise;
import hy.tmc.cli.domain.Review;
import hy.tmc.cli.domain.submission.SubmissionResult;
import hy.tmc.core.exceptions.ProtocolException;
import java.io.IOException;
import java.util.ArrayList;

import java.util.Arrays;
import java.util.List;

/**
 * A Utility class for handling JSONs downloaded from the TMC-server.
 */
public class TmcJsonParser {

    /**
     * Get list of all the courses on the server specified by ServerData.
     *
     * @param courseAddress address of json
     * @return List of Course-objects
     */
    public static List<Course> getCourses(String courseAddress) throws IOException {
        JsonObject jsonObject = getJsonFrom(courseAddress);
        Gson mapper = new Gson();
        Course[] courses = mapper
                .fromJson(jsonObject.getAsJsonArray("courses"), Course[].class);
        return Arrays.asList(courses);
    }

    /**
     * Get JSON-data from url.
     *
     * @param url url from which the object data is fetched
     * @return JSON-object
     */
    private static JsonObject getJsonFrom(String url) throws IOException {
        HttpResult httpResult = UrlCommunicator.makeGetRequest(
                url, ClientData.getFormattedUserData()
        );
        String data = httpResult.getData();
        return new JsonParser().parse(data).getAsJsonObject();
    }

    /**
     * Get list of all the courses on the server specified by ServerData.
     *
     * @return List of Course-objects
     */
    public static List<Course> getCourses() throws IOException, ProtocolException {
        return getCourses(new ConfigHandler().readCoursesAddress());
    }

    /**
     * Maps the JSON as review-objects.
     *
     * @param reviewUrl which is found from course-object
     * @return List of reviews
     */
    public static List<Review> getReviews(String reviewUrl) throws IOException {
        JsonObject jsonObject = getJsonFrom(withApiVersion(reviewUrl));
        Gson mapper = new Gson();
        Review[] reviews = mapper
                .fromJson(jsonObject.getAsJsonArray("reviews"), Review[].class);
        return Arrays.asList(reviews);
    }

    /**
     * Get all exercise names of a course specified by courseUrl.
     *
     * @param courseUrl url of the course we are interested in
     * @return String of all exercise names separated by newlines
     */
    public static String getExerciseNames(String courseUrl) throws IOException {
        List<Exercise> exercises = getExercises(courseUrl);
        StringBuilder asString = new StringBuilder();
        for (Exercise exercise : exercises) {
            asString.append(exercise.getName());
            asString.append("\n");
        }
        return asString.toString();
    }
    
     /**
     * Reads courses from string.
     */
    public static List<Course> getCoursesFromString(String jsonString) {
        JsonObject jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();
        Gson mapper = new Gson();
        Course[] courses = mapper
                .fromJson(jsonObject.getAsJsonArray("courses"), Course[].class);
        return Arrays.asList(courses);
    }


    /**
     * Get information about course specified by the course ID.
     *
     * @param courseID
     * @return an course Object (parsed from JSON)
     */
    public static Optional<Course> getCourse(int courseID) throws IOException, ProtocolException {
        ConfigHandler confighandler = new ConfigHandler();
        if (!courseExists(courseID)) {
            return Optional.absent();
        }
        return getCourse(confighandler.getCourseUrl(courseID));
    }

    private static boolean courseExists(int courseID) throws IOException, ProtocolException {
        List<Course> allCourses = getCourses();
        for (Course course : allCourses) {
            if (course.getId() == courseID) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get information about course specified by the URL path to course JSON.
     *
     * @param courseUrl URL path to course JSON
     * @return an Course object (parsed from JSON)
     */
    public static Optional<Course> getCourse(String courseUrl) throws IOException {
        JsonObject courseJson = getJsonFrom(courseUrl);
        Gson mapper = new Gson();
        Course course = mapper.fromJson(courseJson.getAsJsonObject("course"), Course.class);

        if (course == null) {
            return Optional.absent();
        }
        return Optional.of(course);

    }

    /**
     * Get all exercises of a course specified by Course.
     *
     * @param course Course that we are interested in
     * @return List of all exercises as Exercise-objects
     */
    public static List<Exercise> getExercises(Course course) throws IOException {
        return getExercises(course.getId());
    }

    /**
     * Get all exercises of a course specified by Course id.
     *
     * @param id id of the course we are interested in
     * @return List of a all exercises as Exercise-objects
     */
    public static List<Exercise> getExercises(int id) throws IOException {
        ConfigHandler confighandler = new ConfigHandler();
        return getExercises(confighandler.getCourseUrl(id));
    }

    /**
     * Get all exercises of a course specified by courseUrl.
     *
     * @param courseUrl url of the course we are interested in
     * @return List of all exercises as Exercise-objects. If no course is found,
     * empty list will be returned.
     */
    public static List<Exercise> getExercises(String courseUrl) throws IOException {
        Optional<Course> course = getCourse(courseUrl);
        if (course.isPresent()) {
            return course.get().getExercises();
        }
        return new ArrayList<>();
    }

    /**
     * Parses JSON in url to create a SubmissionResult object.
     *
     * @param url to make request to
     * @return A SubmissionResult object which contains data of submission.
     */
    public static SubmissionResult getSubmissionResult(String url) throws IOException {
        JsonObject submission = getJsonFrom(url);
        Gson mapper = new Gson();
        return mapper.fromJson(submission, SubmissionResult.class);
    }

    /**
     * Parses the submission result URL from a HttpResult with JSON.
     *
     * @param result HTTPResult containing JSON with submission url.
     * @return url where submission results are located.
     */
    public static String getSubmissionUrl(HttpResult result) {
        return getPropertyFromResult(result, "submission_url");
    }

    /**
     * Parses the submission result paste URL from a HttpResult with JSON.
     *
     * @param result HTTPResult containing JSON with paste url.
     * @return url where paste is located.
     */
    public static String getPasteUrl(HttpResult result) {
        return getPropertyFromResult(result, "paste_url");
    }

    private static String getPropertyFromResult(HttpResult result, String property) {
        JsonElement jelement = new JsonParser().parse(result.getData());
        JsonObject jobject = jelement.getAsJsonObject();
        return jobject.get(property).getAsString();
    }

    private static String withApiVersion(String url) {
        String postfix = "?api_version=" + ConfigHandler.apiVersion;
        if (!url.endsWith(postfix)) {
            url += postfix;
        }
        return url;
    }
}
