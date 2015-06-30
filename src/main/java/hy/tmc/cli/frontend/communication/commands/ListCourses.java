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

    @Override
    public List<Course> call() throws ProtocolException, IOException {
        checkData();
        List<Course> courses = TmcJsonParser.getCourses();
        return courses;
    }
}
