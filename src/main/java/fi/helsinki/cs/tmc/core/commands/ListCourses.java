package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.TmcApi;
import fi.helsinki.cs.tmc.core.communication.UrlCommunicator;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;

import java.io.IOException;
import java.util.List;

public class ListCourses extends Command<List<Course>> {

    private TmcApi tmcApi;

    public ListCourses(TmcSettings settings) {
        super(settings);
        this.tmcApi = new TmcApi(settings);
    }

    public ListCourses(TmcSettings settings, TmcApi tmcApi) {
        super(settings);
        this.tmcApi = tmcApi;
    }

    public ListCourses(TmcSettings settings, UrlCommunicator communicator) {
        super(settings);
        this.tmcApi = new TmcApi(communicator, settings);
    }

    @Override
    public List<Course> call() throws TmcCoreException, IOException {
        if (!settings.userDataExists()) {
            throw new TmcCoreException("User must be authorized first");
        }
        return tmcApi.getCourses();
    }
}
