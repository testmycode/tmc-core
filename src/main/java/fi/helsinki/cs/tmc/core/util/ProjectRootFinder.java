package fi.helsinki.cs.tmc.core.util;

import fi.helsinki.cs.tmc.core.communication.TmcApi;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.exceptions.InvalidExerciseDirectoryException;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;
import fi.helsinki.cs.tmc.langs.util.TaskExecutor;
import fi.helsinki.cs.tmc.langs.util.TaskExecutorImpl;

import com.google.common.base.Optional;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * A helper class that searches for a project root directory.
 */
public class ProjectRootFinder implements RootFinder {

    private final TaskExecutor tmcLangs;
    private final TmcApi tmcApi;

    public ProjectRootFinder(TaskExecutor tmcLangs, TmcApi tmcApi) {
        this.tmcLangs = tmcLangs;
        this.tmcApi = tmcApi;
    }

    public ProjectRootFinder(TmcApi tmcApi) {
        this(new TaskExecutorImpl(), tmcApi);
    }

    /**
     * Get the path of the project root directory.
     *
     * @param start the path of the extracted zip.
     * @return path of the root, of null if no root was found
     */
    @Override
    public Path getExerciseRoot(Path start) throws InvalidExerciseDirectoryException {
        Path path = start.toAbsolutePath();
        while (path != null) {
            if (tmcLangs.isExerciseRootDirectory(path)) {
                return path;
            }
            path = path.getParent();
        }
        throw new InvalidExerciseDirectoryException();
    }

    /**
     * Returns a course object. Finds it from the current path.
     *
     * @param path Path to look up course from.
     * @return Course-object containing information of the course found.
     */
    @Override
    public Optional<Course> getCourse(Path path) throws IOException, TmcCoreException {
        List<Course> courses = tmcApi.getCourses();
        while (path != null) {
            for (Course course : courses) {
                if (course.getName().equals(path.getFileName().toString())) {
                    return Optional.of(course);
                }
            }
            path = path.getParent();
        }
        return Optional.absent();
    }
}
