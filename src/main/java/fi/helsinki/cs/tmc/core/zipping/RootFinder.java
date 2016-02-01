package fi.helsinki.cs.tmc.core.zipping;

import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;

import com.google.common.base.Optional;

import java.io.IOException;
import java.nio.file.Path;

public interface RootFinder {

    // remove optional
    Optional<Path> getRootDirectory(Path zipRoot);


    // Ok stuff, just move away from here
    Optional<Course> getCurrentCourse(Path path) throws IOException, TmcCoreException;
}
