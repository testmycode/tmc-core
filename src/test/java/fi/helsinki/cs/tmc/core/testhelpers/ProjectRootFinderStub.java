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

public class ProjectRootFinderStub implements RootFinder {

    private String returnValue;
    private TmcApi tmcApi;
    private HashMap<String, Course> courseStubs;

    public ProjectRootFinderStub(TmcApi tmcApi) {
        this.returnValue = "";
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

    public String getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(String returnValue) {
        this.returnValue = returnValue;
    }

    @Override
    public Optional<Path> getRootDirectory(Path zipRoot) {
        return Optional.of(Paths.get(returnValue));
    }

    public Optional<Course> getCurrentCourse(String path) {
        String[] folders = path.split("\\" + File.separator);

        for (String folder : folders) {
            if (courseStubs.containsKey(folder)) {
                return Optional.of(courseStubs.get(folder));
            }
        }
        return Optional.absent();
    }
}
