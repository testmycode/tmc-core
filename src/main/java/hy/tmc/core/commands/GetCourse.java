package hy.tmc.core.commands;

import com.google.common.base.Optional;
import hy.tmc.core.communication.TmcJsonParser;
import hy.tmc.core.communication.UrlHelper;
import hy.tmc.core.configuration.TmcSettings;
import hy.tmc.core.domain.Course;
import hy.tmc.core.exceptions.TmcCoreException;
import java.io.IOException;

public class GetCourse extends Command<Course> {

    private TmcJsonParser jsonParser;
    private String path;
    
    public GetCourse(TmcSettings settings, String path) {
        super(settings);
        this.jsonParser = new TmcJsonParser(settings);
        this.path = path;
    }

    @Override
    public void checkData() throws TmcCoreException, IOException {
        validate(this.settings.getUsername(), "username must be set!");
        validate(this.settings.getPassword(), "password must be set!");
    }

    private void validate(String field, String message) throws TmcCoreException {
        if (field == null || field.isEmpty()) {
            throw new TmcCoreException(message);
        }
    }

    @Override
    public Course call() throws Exception {
        String url = new UrlHelper(settings).withApiVersion(path);
        Optional<Course> course = jsonParser.getCourse(url);
        if (!course.isPresent()) {
            throw new TmcCoreException("No course found by specified url: " + url);
        }
        return course.get();
    }
}
