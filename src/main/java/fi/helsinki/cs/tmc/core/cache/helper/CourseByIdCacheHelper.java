package fi.helsinki.cs.tmc.core.cache.helper;

import fi.helsinki.cs.tmc.core.cache.Cache;
import fi.helsinki.cs.tmc.core.cache.CourseCache;
import fi.helsinki.cs.tmc.core.communication.TmcApi;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;

import com.google.common.base.Optional;

import java.io.IOException;
import java.net.URISyntaxException;

public class CourseByIdCacheHelper extends AbstractCacheHelper<Integer, Course> {

    private final TmcApi tmcApi;
    private final CourseCache cache;

    public CourseByIdCacheHelper(Cache.QueryStrategy queryStrategy, CourseCache cache, TmcApi tmcApi) {
        super(queryStrategy);

        this.cache = cache;
        this.tmcApi = tmcApi;
    }

    @Override
    protected Optional<Course> getFromServer(Integer courseId) throws TmcCoreException {
        Optional<Course> course;

        try {
            course = tmcApi.getCourse(courseId);
        } catch (IOException | URISyntaxException e) {
            throw new TmcCoreException("Failed to fetch current course's details from server.", e);
        }

        if (course.isPresent()) {
            try {
                cache.put(courseId, course.get());
            } catch (IOException e) {
                throw new TmcCoreException("Failed to cache retrieved course", e);
            }

            return course;
        }

        throw new TmcCoreException("Failed to fetch current course's details from server.");
    }

    @Override
    protected void cache(Integer courseId, Course course) throws TmcCoreException {
        try {
            cache.put(courseId, course);
        } catch (IOException e) {
            throw new TmcCoreException("Failed to store course in local cache", e);
        }
    }

    @Override
    protected Optional<Course> getFromCache(Integer courseId) {
        try {
            return Optional.of(cache.get(courseId));
        } catch (IOException e) {
            return Optional.absent();
        }
    }
}
