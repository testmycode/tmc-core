package fi.helsinki.cs.tmc.core.util;

import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.exceptions.InvalidExerciseDirectoryException;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;

import com.google.common.base.Optional;

import java.io.IOException;
import java.nio.file.Path;

public interface RootFinder {

    Path getExerciseRoot(Path zipRoot) throws InvalidExerciseDirectoryException;

    Optional<Course> getCourse(Path path) throws IOException, TmcCoreException;
}
