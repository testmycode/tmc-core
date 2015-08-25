package fi.helsinki.cs.tmc.core.cache.inmemory;

import fi.helsinki.cs.tmc.core.cache.CourseCache;
import fi.helsinki.cs.tmc.core.domain.Course;

public class InMemoryCourseCache extends InMemoryKeyValueCache<Integer, Course> implements CourseCache {

}
