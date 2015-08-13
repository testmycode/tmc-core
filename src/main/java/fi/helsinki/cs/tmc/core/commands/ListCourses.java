package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.TmcJsonParser;
import fi.helsinki.cs.tmc.core.communication.UrlCommunicator;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;

import java.io.IOException;
import java.util.List;

public class ListCourses extends Command<List<Course>> {

    private TmcJsonParser parser;

    public ListCourses(TmcSettings settings) {
        super(settings);
        this.parser = new TmcJsonParser(settings);
    }

    public ListCourses(TmcSettings settings, TmcJsonParser parser) {
        super(settings);
        this.parser = parser;
    }

    public ListCourses(TmcSettings settings, UrlCommunicator communicator) {
        super(settings);
        this.parser = new TmcJsonParser(communicator, settings);
    }

    @Override
    public List<Course> call() throws TmcCoreException, IOException {
        if (!settings.userDataExists()) {
            throw new TmcCoreException("User must be authorized first");
        }
        return parser.getCourses();
    }
}
