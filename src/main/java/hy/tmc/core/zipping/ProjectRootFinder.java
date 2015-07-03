package hy.tmc.core.zipping;

import com.google.common.base.Optional;
import hy.tmc.core.communication.TmcJsonParser;
import hy.tmc.core.configuration.TmcSettings;
import hy.tmc.core.domain.Course;
import hy.tmc.core.exceptions.TmcCoreException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class ProjectRootFinder implements RootFinder {

    private final ProjectRootDetector detector;
    private TmcSettings settings;

    /**
     * A helper class that searches for a project root directory. It must be given a
     * ProjectRootDetector that corresponds with the project in question. For example, for a Maven
     * project a DefaultRootDetector can be used.
     *
     * @param detector the RootDetector that specifies if a directory is a project root
     */
    public ProjectRootFinder(ProjectRootDetector detector, TmcSettings settings) {
        this.detector = detector;
        this.settings = settings;
    }

    /**
     * Get the path of the project root directory.
     *
     * @param start the path of the extracted zip.
     * @return path of the root, of null if no root was found
     */
    @Override
    public Optional<Path> getRootDirectory(Path start) {
        return search(start);
    }

    private Optional<Path> search(Path path) {
        while (path.getParent() != null) {
            if (detector.isRootDirectory(path)) {
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
    public Optional<Course> getCurrentCourse(String path) throws IOException {
        String[] foldersOfPwd = path.split(File.separator);
        try {
            checkPwd(foldersOfPwd);
        } catch (TmcCoreException ex) {
            return Optional.absent();
        }
        return findCourseByPath(foldersOfPwd);
    }

    /**
     * Downloads all courses and iterates over them. Returns Course whose name matches with one
     * folder in given path.
     *
     * @param foldersPath contains the names of the folders in path
     * @return Course
     */
    public Optional<Course> findCourseByPath(String[] foldersPath) throws IOException {
        List<Course> courses = TmcJsonParser.getCourses(settings.getServerAddress());
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
