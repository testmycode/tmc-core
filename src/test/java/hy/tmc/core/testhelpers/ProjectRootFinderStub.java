package hy.tmc.core.testhelpers;

import com.google.common.base.Optional;
import hy.tmc.core.communication.TmcJsonParser;
import hy.tmc.core.configuration.TmcSettings;
import hy.tmc.core.domain.Course;
import hy.tmc.core.zipping.RootFinder;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

public class ProjectRootFinderStub implements RootFinder {

    private String returnValue;
    private HashMap<String, Course> courseStubs;
    private TmcJsonParser jsonParser;

    public ProjectRootFinderStub(TmcJsonParser jsonParser) {
        this.returnValue = "";
        courseStubs = new HashMap<>();
        this.jsonParser = jsonParser;
        fillCourseStubs();
    }

    private void fillCourseStubs() {
        String allCourses = ExampleJson.allCoursesExample;
        List<Course> courses = jsonParser.getCoursesFromString(allCourses);
        for (Course c:courses) {
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
        String[] folders = path.split(File.separator);

        for(String folder:folders) {
            if(courseStubs.containsKey(folder)) {
                return Optional.of(courseStubs.get(folder));
            }
        }
        return Optional.absent();
    }
}
