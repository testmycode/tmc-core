package fi.helsinki.cs.tmc.core.cache.helper;

import fi.helsinki.cs.tmc.core.cache.Cache;
import fi.helsinki.cs.tmc.core.cache.CourseCache;
import fi.helsinki.cs.tmc.core.communication.TmcApi;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;

import com.google.common.base.Optional;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class CourseByNameCacheHelper extends AbstractCacheHelper<String, Course> {

    private final TmcApi tmcApi;
    private final CourseCache cache;

    public CourseByNameCacheHelper(Cache.QueryStrategy queryStrategy, CourseCache cache, TmcApi tmcApi) {
        super(queryStrategy);

        this.cache = cache;
        this.tmcApi = tmcApi;
    }

    @Override
    protected Optional<Course> getFromServer(String name) throws TmcCoreException {

        List<Course> serverCourses;
        try {
            serverCourses = tmcApi.getCourses();
        } catch (IOException ex) {
            throw new TmcCoreException("Failed to retrieve courses from server", ex);
        }

        for (Course course : serverCourses) {
            if (course.getName().equals(name)) {
                cache(name, course);
                return Optional.of(course);
            }
        }

        return Optional.absent();
    }

    @Override
    protected Optional<Course> getFromCache(String name) throws TmcCoreException {
        Collection<Course> courses;
        try {
            courses = cache.values();
        } catch (IOException ex) {
            throw new TmcCoreException("Failed to read courses from cache", ex);
        }

        for (Course course : courses) {
            if (course.getName().equals(name)) {
                return Optional.of(course);
            }
        }

        return Optional.absent();
    }

    @Override
    protected void cache(String name, Course course) throws TmcCoreException {
        try {
            cache.put(course.getId(), course);
        } catch (IOException ex) {
            throw new TmcCoreException("Failed to write course to cache", ex);
        }
    }
}
