package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.TmcApi;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;

import java.io.IOException;
import java.util.List;

/**
 * A {@link Command} for retrieving the course list from the server.
 */
public class ListCourses extends Command<List<Course>> {

    private TmcApi tmcApi;

    /**
     * Constructs a new list courses command with {@code settings}.
     */
    public ListCourses(TmcSettings settings) {
        this(settings, new TmcApi(settings));
    }

    /**
     * Constructs a new list courses command with {@code settings} that uses {@code tmcApi} to
     * communicate with the server.
     */
    public ListCourses(TmcSettings settings, TmcApi tmcApi) {
        super(settings);
        this.tmcApi = tmcApi;
    }

    /**
     * Entry point for launching this command.
     */
    @Override
    public List<Course> call() throws TmcCoreException {
        if (!settings.userDataExists()) {
            throw new TmcCoreException("User must be authorized first");
        }

        try {
            return tmcApi.getCourses();
        } catch (IOException ex) {
            throw new TmcCoreException("Failed to fetch courses from server. Check your credentials and server address.", ex);
        }
    }
}
