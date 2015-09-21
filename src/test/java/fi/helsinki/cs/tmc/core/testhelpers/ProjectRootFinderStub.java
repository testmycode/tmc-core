package fi.helsinki.cs.tmc.core.testhelpers;

import fi.helsinki.cs.tmc.core.communication.TmcApi;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.zipping.RootFinder;

import com.google.common.base.Optional;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectRootFinderStub implements RootFinder {

    private Path returnValue;
    private TmcApi tmcApi;
    private Map<String, Course> courseStubs;

    public ProjectRootFinderStub(TmcApi tmcApi) {
        this.returnValue = Paths.get("");
        this.tmcApi = tmcApi;
        this.courseStubs = new HashMap<>();
        fillCourseStubs();
    }

    private void fillCourseStubs() {
        String allCourses = ExampleJson.allCoursesExample;
        List<Course> courses = tmcApi.getCoursesFromString(allCourses);
        for (Course c : courses) {
            courseStubs.put(c.getName(), c);
        }
    }

    public Path getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(Path returnValue) {
        this.returnValue = returnValue;
    }

    @Override
    public Optional<Path> getRootDirectory(Path zipRoot) {
        return Optional.of(returnValue);
    }

    public Optional<Course> getCurrentCourse(Path path) {
        String[] folders = path.toString().split("\\" + File.separator);

        for (String folder : folders) {
            if (courseStubs.containsKey(folder)) {
                return Optional.of(courseStubs.get(folder));
            }
        }
        return Optional.absent();
    }
}
