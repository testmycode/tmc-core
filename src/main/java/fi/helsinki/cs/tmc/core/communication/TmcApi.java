package fi.helsinki.cs.tmc.core.communication;

import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.Review;
import fi.helsinki.cs.tmc.core.domain.submission.SubmissionResult;

import com.google.common.base.Optional;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A Utility class for handling JSONs downloaded from the TMC-server.
 */
public class TmcApi {

    private UrlCommunicator urlCommunicator;
    private TmcSettings settings;
    private UrlHelper helper;

    public TmcApi(TmcSettings settings) {
        this.settings = settings;
        this.helper = new UrlHelper(settings);
        this.urlCommunicator = new UrlCommunicator(settings);
    }

    public TmcApi(UrlCommunicator urlCommunicator, TmcSettings settings) {
        this.urlCommunicator = urlCommunicator;
        this.settings = settings;
        this.helper = new UrlHelper(settings);
    }

    /**
     * Get list of all the courses on the server specified by ServerData.
     *
     * @return List of Course-objects
     */
    public List<Course> getCourses() throws IOException {
        String coursesAddress = helper.allCoursesAddress(settings.getServerAddress());
        JsonObject jsonObject = getJsonFrom(URI.create(coursesAddress));
        Gson mapper = new Gson();
        Course[] courses = mapper.fromJson(jsonObject.getAsJsonArray("courses"), Course[].class);
        return Arrays.asList(courses);
    }

    /**
     * Get JSON-data from url.
     *
     * @param url url from which the object data is fetched
     * @return JSON-object
     */
    private JsonObject getJsonFrom(URI url) throws IOException {
        HttpResult httpResult = urlCommunicator.makeGetRequestWithAuthentication(url);
        if (httpResult == null) {
            return null;
        }
        String data = httpResult.getData();
        return new JsonParser().parse(data).getAsJsonObject();
    }

    /**
     * Get String from url.
     *
     * @param url url from which the data is fetched
     * @return JSON-object
     */
    @Deprecated
    protected String getRawTextFrom(URI url) throws IOException {
        // TODO: Remove
        HttpResult httpResult = urlCommunicator.makeGetRequestWithAuthentication(url);
        if (httpResult == null) {
            return null;
        }
        return httpResult.getData();
    }

    /**
     * Maps the JSON as review-objects.
     *
     * @param reviewUrl which is found from course-object
     * @return List of reviews
     */
    public List<Review> getReviews(URI reviewUrl) throws IOException, URISyntaxException {
        JsonObject jsonObject = getJsonFrom(helper.withParams(reviewUrl));
        Gson mapper = new Gson();
        Review[] reviews = mapper.fromJson(jsonObject.getAsJsonArray("reviews"), Review[].class);
        return Arrays.asList(reviews);
    }

    /**
     * Reads courses from string.
     */
    @Deprecated
    //TODO: Remove
    public List<Course> getCoursesFromString(String jsonString) {
        JsonObject jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();
        Gson mapper = new Gson();
        Course[] courses = mapper.fromJson(jsonObject.getAsJsonArray("courses"), Course[].class);
        return Arrays.asList(courses);
    }

    /**
     * Reads one course from string.
     */
    @Deprecated
    //TODO: Remove
    public Course getCourseFromString(String jsonString) {
        JsonObject jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();
        Gson mapper = new Gson();
        return mapper.fromJson(jsonObject.get("course"), Course.class);
    }

    /**
     * Get information about course specified by the course ID.
     *
     * @return an course Object (parsed from JSON)
     */
    public Optional<Course> getCourse(int courseId) throws IOException, URISyntaxException {
        List<Course> allCourses = getCourses();
        for (Course course : allCourses) {
            if (course.getId() == courseId) {
                return getCourse(helper.getCourseUrl(course));
            }
        }
        return Optional.absent();
    }

    /**
     * Get information about course specified by the URL path to course JSON.
     *
     * @param courseUrl URL path to course JSON
     * @return an Course object (parsed from JSON)
     */
    //TODO: Replace with getCourse(int courseId) ?
    @Deprecated
    public Optional<Course> getCourse(URI courseUrl) throws IOException {
        JsonObject courseJson = getJsonFrom(courseUrl);
        if (courseJson == null) {
            return Optional.absent();
        }
        Gson mapper = new Gson();
        Course course = mapper.fromJson(courseJson.getAsJsonObject("course"), Course.class);

        if (course == null) {
            return Optional.fromNullable(course);
        }

        for (Exercise e : course.getExercises()) {
            e.setCourseName(course.getName());
        }

        return Optional.of(course);
    }

    /**
     * Get all exercises of a course specified by Course.
     *
     * @param course Course that we are interested in
     * @return List of all exercises as Exercise-objects
     */
    public List<Exercise> getExercisesFromServer(Course course)
            throws IOException, URISyntaxException {
        return getExercises(course.getId());
    }

    /**
     * Get all exercises of a course specified by Course id.
     *
     * @param id id of the course we are interested in
     * @return List of a all exercises as Exercise-objects
     */
    public List<Exercise> getExercises(int id) throws IOException, URISyntaxException {
        Optional<Course> courseOptional = getCourse(id);
        if (courseOptional.isPresent()) {
            Course course = courseOptional.get();
            for (Exercise exercise : course.getExercises()) {
                exercise.setCourseName(course.getName());
            }
            return course.getExercises();
        }
        return new ArrayList<>();
    }
}
