package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.TmcServerCommunicationTaskFactory;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;
import fi.helsinki.cs.tmc.core.utilities.ServerErrorHelper;

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
        logger.info("Retrieving course list");
        informObserver(0, "Retrieving course list");
        try {
            List<Course> result = tmcServerCommunicationTaskFactory
                                        .getDownloadingCourseListTask()
                                        .call();
            informObserver(1, "Successfully fetched course list");
            logger.debug("Successfully fetched course list");
            return result;
        } catch (Exception ex) {
            logger.info("Failed to fetch courses from the server", ex);
            informObserver(1, "Failed to fetch courses from the server");
            throw new TmcCoreException("Failed to fetch courses from the server. \n"
                + ServerErrorHelper.getServerExceptionMsg(ex), ex);
        }
    }
}
