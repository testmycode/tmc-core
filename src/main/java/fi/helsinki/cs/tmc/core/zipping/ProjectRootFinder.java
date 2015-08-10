package fi.helsinki.cs.tmc.core.zipping;

import fi.helsinki.cs.tmc.core.communication.TmcJsonParser;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;

import com.google.common.base.Optional;

import fi.helsinki.cs.tmc.langs.util.TaskExecutor;
import fi.helsinki.cs.tmc.langs.util.TaskExecutorImpl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class ProjectRootFinder implements RootFinder {

    private final TaskExecutor langs;
    private TmcJsonParser tmcJsonParser;

    /**
     * A helper class that searches for a project root directory.
     */
    public ProjectRootFinder(TaskExecutor langs, TmcJsonParser jsonParser) {
        this.langs = langs;
        this.tmcJsonParser = jsonParser;
    }

    public ProjectRootFinder(TmcJsonParser jsonParser) {
        this(new TaskExecutorImpl(), jsonParser);
    }

    /**
     * Get the path of the project root directory.
     *
     * @param start the path of the extracted zip.
     * @return path of the root, of null if no root was found
     */
    @Override
    public Optional<Path> getRootDirectory(Path start) {
        return search(start.toAbsolutePath());
    }

    private Optional<Path> search(Path path) {
        while (path.getParent() != null) {
            if (path.endsWith("tmc-core")) {
                path = path.getParent();
                continue;
            }
            if (langs.isExerciseRootDirectory(path)) {
                return Optional.of(path);
            }
            path = path.getParent();
        }
        return Optional.absent();
    }

    /**
     * Returns a course object. Finds it from the current path.
     *
     * @param path Path to look up course from.
     * @return Course-object containing information of the course found.
     */
    @Override
    public Optional<Course> getCurrentCourse(String path) throws IOException, TmcCoreException {
        String[] foldersOfWorkingDirectory = path.split("\\" + File.separator);
        try {
            checkPwd(foldersOfWorkingDirectory);
        } catch (TmcCoreException ex) {
            return Optional.absent();
        }
        return findCourseByPath(foldersOfWorkingDirectory);
    }

    /**
     * Downloads all courses and iterates over them. Returns Course whose name matches with one
     * folder in given path.
     *
     * @param foldersPath contains the names of the folders in path
     * @return Course
     */
    public Optional<Course> findCourseByPath(String[] foldersPath)
            throws IOException, TmcCoreException {
        List<Course> courses = tmcJsonParser.getCourses();
        for (Course course : courses) {
            for (String folderName : foldersPath) {
                if (course.getName().equals(folderName)) {
                    Optional<Course> courseOptional = Optional.of(course);
                    return courseOptional;
                }
            }
        }
        return Optional.absent();
    }

    private void checkPwd(String[] foldersOfPwd) throws TmcCoreException {
        if (foldersOfPwd.length == 0) {
            throw new TmcCoreException("No folders found from the path.");
        }
    }
}
