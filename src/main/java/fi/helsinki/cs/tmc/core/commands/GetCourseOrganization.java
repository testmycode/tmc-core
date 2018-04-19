package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.TmcServerCommunicationTaskFactory;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Organization;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.exceptions.NotLoggedInException;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;

public class GetCourseOrganization extends Command<Organization> {
    private Course course;

    public GetCourseOrganization(ProgressObserver observer, Course course) {
        super(observer);
        this.course = course;
    }

    @VisibleForTesting
    public GetCourseOrganization(ProgressObserver observer, Course course, TmcServerCommunicationTaskFactory tmcServerCommunicationTaskFactory) {
        super(observer, tmcServerCommunicationTaskFactory);
        this.course = course;
    }

    @Override
    public Organization call() throws Exception {
        informObserver(0, "Fetching course");
        try {
            Optional<Course> result = tmcServerCommunicationTaskFactory
                    .getCourseFromAllCoursesByIdTask(course.getId());
            if (!result.isPresent()) {
                throw new TmcCoreException();
            }
            informObserver(0.5, "Fetching course completed successfully");
            Organization organization = tmcServerCommunicationTaskFactory.getOrganizationBySlug(result.get().getOrganizationSlug());
            informObserver(1, "Fetching organization completed successfully");
            return organization;
        } catch (Exception ex) {
            if (ex instanceof NotLoggedInException) {
                throw ex;
            }
            informObserver(1, "Failed to fetch organization");
            throw new TmcCoreException("Failed to fetch organization", ex);
        }
    }
}
