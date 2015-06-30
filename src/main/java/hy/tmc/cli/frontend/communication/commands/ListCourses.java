package hy.tmc.cli.frontend.communication.commands;

import com.google.common.base.Optional;
import hy.tmc.cli.backend.communication.TmcJsonParser;
import hy.tmc.cli.configuration.ClientData;
import hy.tmc.cli.domain.Course;
import hy.tmc.core.exceptions.ProtocolException;
import java.io.IOException;
import java.util.List;

public class ListCourses extends Command<List<Course>> {

    
    public ListCourses(){
        
    }
    /**
     * Checks that the user has authenticated, by verifying ClientData.
     *
     * @throws ProtocolException if ClientData is empty
     */
    @Override
    public void checkData() throws ProtocolException {
        if (!ClientData.userDataExists()) {
            throw new ProtocolException("User must be authorized first");
        }
    }

    private int getLongest(List<Course> courses) {
        int longest = courses.get(0).getName().length();
        for (Course course : courses) {
            longest = Math.max(course.getName().length(), longest);
        }
        return longest;
    }

    private String getCourseNames(List<Course> courses) {
        StringBuilder result = new StringBuilder();

        for (Course course : courses) {
            String name = course.getName();
            result.append(name).append(", ");
            result = addSpaces(result, name, getLongest(courses))
                    .append("id:")
                    .append(course.getId());
            result.append("\n");
        }

        return result.toString();
    }

    private StringBuilder addSpaces(StringBuilder result, String name, int longest) {
        int spaces = longest - name.length();
        for (int i = 0; i < spaces; i++) {
            result.append(" ");
        }
        return result;
    }

    @Override
    public List<Course> call() throws ProtocolException, IOException {
        checkData();
        List<Course> courses = TmcJsonParser.getCourses();
        return courses;
    }
}
