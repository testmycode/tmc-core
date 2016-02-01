package fi.helsinki.cs.tmc.core.zipping;

import fi.helsinki.cs.tmc.core.communication.TmcApi;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;
import fi.helsinki.cs.tmc.langs.util.TaskExecutor;
import fi.helsinki.cs.tmc.langs.util.TaskExecutorImpl;

import com.google.common.base.Optional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

// TODO: can't we get this from tmc-langs?

public class ProjectRootFinder implements RootFinder {

    private final TaskExecutor tmcLangs;
    private TmcApi tmcApi;

    /**
     * A helper class that searches for a project root directory.
     */
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
    public Optional<Path> getRootDirectory(Path start) {
        return search(start.toAbsolutePath());
    }

    private Optional<Path> search(Path path) {
        while (path.getParent() != null) {
            if (tmcLangs.isExerciseRootDirectory(path)) {
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
    public Optional<Course> getCurrentCourse(Path path) throws IOException, TmcCoreException {
        String[] foldersOfWorkingDirectory = path.toString().split("\\" + File.separator);
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
        List<Course> courses = tmcApi.getCourses();
        for (Course course : courses) {
            for (String folderName : foldersPath) {
                if (course.getName().equals(folderName)) {
                    return Optional.of(course);
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
