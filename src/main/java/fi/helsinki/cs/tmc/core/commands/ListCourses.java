package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.TmcServerCommunicationTaskFactory;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;

import com.google.common.annotations.VisibleForTesting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * A {@link Command} for retrieving the course list from the server.
 */
public class ListCourses extends Command<List<Course>> {

    private static final Logger logger = LoggerFactory.getLogger(ListCourses.class);

    public ListCourses(ProgressObserver observer) {
        super(observer);
    }

    @VisibleForTesting
    ListCourses(
            ProgressObserver observer,
            TmcServerCommunicationTaskFactory tmcServerCommunicationTaskFactory) {
        super(observer, tmcServerCommunicationTaskFactory);
    }

    @Override
    public List<Course> call() throws TmcCoreException {
        try {
            return tmcServerCommunicationTaskFactory.getDownloadingCourseListTask().call();
        } catch (Exception ex) {
            logger.warn("Failed to fetch courses from the server", ex);
            throw new TmcCoreException("Failed to fetch courses from the server", ex);
        }
    }
}
