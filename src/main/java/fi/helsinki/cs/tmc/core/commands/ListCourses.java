package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.cache.Cache;
import fi.helsinki.cs.tmc.core.cache.CourseCache;
import fi.helsinki.cs.tmc.core.communication.TmcApi;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A {@link Command} for retrieving the course list from the server.
 */
public class ListCourses extends Command<List<Course>> {

    private final TmcApi tmcApi;
    private final CourseCache cache;
    private final Cache.QueryStrategy queryStrategy;

    /**
     * Constructs a new list courses command with {@code settings}.
     */
    public ListCourses(TmcSettings settings, CourseCache cache, Cache.QueryStrategy queryStrategy) {
        this(settings, cache, queryStrategy, new TmcApi(settings));
    }

    /**
     * Constructs a new list courses command with {@code settings} that uses {@code tmcApi} to
     * communicate with the server.
     */
    public ListCourses(TmcSettings settings, CourseCache cache, Cache.QueryStrategy queryStrategy, TmcApi tmcApi) {
        super(settings);
        this.cache = cache;
        this.queryStrategy = queryStrategy;

        this.tmcApi = tmcApi;
    }

    /**
     * Entry point for launching this command.
     */
    @Override
    public List<Course> call() throws TmcCoreException {
        if (!settings.userDataExists()) {
            throw new TmcCoreException("User must be authorized first");
        }

        if (queryStrategy == Cache.QueryStrategy.FORCE_UPDATE) {
            List<Course> courses = getFromServer();
            cache(courses);
            return courses;
        }

        if (queryStrategy == Cache.QueryStrategy.PREFER_LOCAL) {
            List<Course> courses = getFromLocalCache();
            if (courses == null || courses.isEmpty()) {
                courses = getFromServer();
                cache(courses);
            }
            return courses;
        }

        List<Course> courses = getFromServer();
        if (courses == null || courses.isEmpty()) {
            courses = getFromLocalCache();
        } else {
            cache(courses);
        }
        return courses;
    }

    private List<Course> getFromServer() throws TmcCoreException {
        try {
            return tmcApi.getCourses();
        } catch (IOException ex) {
            throw new TmcCoreException("Failed to fetch courses from server", ex);
        }
    }

    private List<Course> getFromLocalCache() throws TmcCoreException {
        Collection<Course> courses;
        try {
            courses = cache.values();
        } catch (IOException e) {
            throw new TmcCoreException("Failed to fetch courses from local cache", e);
        }
        if (courses == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(courses);
    }

    private void cache(List<Course> courses) throws TmcCoreException {
        ConcurrentMap<Integer, Course> newCache = new ConcurrentHashMap<>();
        for (Course course : courses) {
            newCache.put(course.getId(), course);
        }
        try {
            cache.clear();
        } catch (IOException ex) {
            throw new TmcCoreException("Failed to clear cache", ex);
        }

        try {
            cache.write(newCache);
        } catch (IOException ex) {
            throw new TmcCoreException("Failed to write cache", ex);
        }
    }
}
