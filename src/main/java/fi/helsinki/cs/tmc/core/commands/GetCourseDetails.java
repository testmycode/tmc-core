package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.TmcServerCommunicationTaskFactory;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;

import com.google.common.annotations.VisibleForTesting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;

/**
 * A {@link Command} for retrieving course details from TMC server.
 */
public class GetCourseDetails extends Command<Course> {

    private static final Logger logger = LoggerFactory.getLogger(GetCourseDetails.class);

    private Course course;

    public GetCourseDetails(ProgressObserver observer, Course course) {
        super(observer);
        this.course = course;
    }

    GetCourseDetails(
            ProgressObserver observer,
            Course course,
            TmcServerCommunicationTaskFactory tmcServerCommunicationTaskFactory) {
        super(observer, tmcServerCommunicationTaskFactory);
        this.course = course;
    }

    @Override
    public Course call() throws TmcCoreException, URISyntaxException {
        logger.info("Fetching course details");
        informObserver(0, "Refreshing course.");
        try {
            Course result = tmcServerCommunicationTaskFactory
                                .getFullCourseInfoTask(course)
                                .call();
            logger.info("Successfully got course details");
            informObserver(1, "Course refresh completed successfully");
            return result;
        } catch (Exception ex) {
            logger.warn("Failed to get course details for course " + course.getName(), ex);
            informObserver(1, "Failed to refresh course");
            throw new TmcCoreException("Failed to get course details", ex);
        }
    }
}
