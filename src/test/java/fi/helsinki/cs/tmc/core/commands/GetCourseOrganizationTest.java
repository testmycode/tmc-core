package fi.helsinki.cs.tmc.core.commands;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fi.helsinki.cs.tmc.core.communication.TmcServerCommunicationTaskFactory;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Organization;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.holders.TmcSettingsHolder;
import fi.helsinki.cs.tmc.core.utils.MockSettings;

import com.google.common.base.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

public class GetCourseOrganizationTest {

    @Mock
    ProgressObserver mockObserver;
    @Mock
    TmcServerCommunicationTaskFactory tmcServerCommunicationTaskFactory;

    TmcSettings settings;
    private Command<Organization> command;
    private Course testCourse;
    private Organization testOrganization;

    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        settings = new MockSettings();
        TmcSettingsHolder.set(settings);
        testOrganization = new Organization("test", "test", "test", "test", false);
        testCourse = new Course("testCourse", testOrganization.getSlug());
        testCourse.setId(0);
        command = new GetCourseOrganization(mockObserver, testCourse, tmcServerCommunicationTaskFactory);
        try {
            when(tmcServerCommunicationTaskFactory.getCourseFromAllCoursesByIdTask(testCourse.getId())).thenReturn(Optional.of(testCourse));
            when(tmcServerCommunicationTaskFactory.getOrganizationBySlug(testCourse.getOrganizationSlug())).thenReturn(testOrganization);
        } catch (Exception e) {
        }
    }

    @Test
    public void callsTmcServerCommunicationTaskFactoryGetCourseByIdFromAllCourses() throws Exception {
        command.call();
        verify(tmcServerCommunicationTaskFactory, times(1)).getCourseFromAllCoursesByIdTask(testCourse.getId());
    }

    @Test
    public void callsTmcServerCommunicationTaskFactoryGetOurganizationBySlug() throws Exception {
        command.call();
        verify(tmcServerCommunicationTaskFactory, times(1)).getOrganizationBySlug(testOrganization.getSlug());
    }

}
