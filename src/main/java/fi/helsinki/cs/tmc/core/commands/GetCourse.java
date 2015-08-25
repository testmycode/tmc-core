package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.cache.helper.CourseByNameCacheHelper;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;

import java.net.URISyntaxException;

/**
 * A {@link Command} for retrieving course details from TMC server.
 */
public class GetCourse extends Command<Course> {

    private final String courseName;
    private final CourseByNameCacheHelper cache;

    /**
     * Constructs a new get course command with {@code settings} for fetching course details for
     * {@code courseName}.
     */
    public GetCourse(String courseName, CourseByNameCacheHelper cache) throws TmcCoreException {
        this.courseName = courseName;
        this.cache = cache;
    }

    /**
     * Entry point for launching this command.
     */
    @Override
    public Course call() throws TmcCoreException, URISyntaxException {
        return cache.get(courseName);
    }
}
