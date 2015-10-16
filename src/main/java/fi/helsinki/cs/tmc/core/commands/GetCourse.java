package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.TmcApi;
import fi.helsinki.cs.tmc.core.communication.UrlHelper;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;

import com.google.common.base.Optional;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * A {@link Command} for retrieving course details from TMC server.
 */
public class GetCourse extends Command<Course> {

    private TmcApi tmcApi;
    private URI url;

    /**
     * Constructs a new get course command with {@code settings} for fetching course details for
     * {@code courseName}.
     */
    public GetCourse(TmcSettings settings, String courseName) throws TmcCoreException {
        super(settings);

        this.tmcApi = new TmcApi(settings);
        this.url = pollServerForCourseUrl(courseName);
    }

    /**
     * Constructs a new get course command with {@code settings} for fetching course details from
     * {@code courseUri}.
     */
    public GetCourse(TmcSettings settings, URI courseUri) {
        super(settings);

        this.tmcApi = new TmcApi(settings);
        this.url = courseUri;
    }

    /**
     * Entry point for launching this command.
     */
    @Override
    public Course call() throws TmcCoreException, URISyntaxException {
        validate(this.settings.getUsername(), "Username must be set!");
        validate(this.settings.getPassword(), "Password must be set!");

        URI urlWithApiVersion = new UrlHelper(settings).withParams(this.url);
        Optional<Course> course;
        try {
            course = tmcApi.getCourse(urlWithApiVersion);
        } catch (IOException ex) {
            throw new TmcCoreException("Failed to fetch course details", ex);
        }

        if (!course.isPresent()) {
            throw new TmcCoreException(
                    "Attempted to fetch nonexistent course " + urlWithApiVersion);
        }

        return course.get();
    }

    private URI pollServerForCourseUrl(String courseName) throws TmcCoreException {
        List<Course> courses;

        try {
            courses = tmcApi.getCourses();
        } catch (IOException e) {
            throw new TmcCoreException("Failed to fetch courses from server:", e);
        }
        for (Course course : courses) {
            if (course.getName().equals(courseName)) {
                return course.getDetailsUrl();
            }
        }
        String errorMessage =
                "There is no course with name "
                        + courseName
                        + " on the server "
                        + settings.getServerAddress();
        throw new TmcCoreException(errorMessage);
    }

    private void validate(String field, String message) throws TmcCoreException {
        if (field == null || field.isEmpty()) {
            throw new TmcCoreException("Failed to fetch course details:" + message);
        }
    }
}
