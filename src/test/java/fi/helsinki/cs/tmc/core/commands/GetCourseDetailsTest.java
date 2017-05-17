package fi.helsinki.cs.tmc.core.commands;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import fi.helsinki.cs.tmc.core.communication.TmcServerCommunicationTaskFactory;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.holders.TmcSettingsHolder;
import fi.helsinki.cs.tmc.core.utils.MockSettings;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.util.concurrent.Callable;
import static com.google.common.truth.Truth.assertThat;

public class GetCourseDetailsTest {

    @Mock ProgressObserver mockObserver;
    @Spy TmcSettings settings = new MockSettings();
    @Mock TmcServerCommunicationTaskFactory factory;
    @Mock Course mockCourse;

    private Command<Course> command;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        TmcSettingsHolder.set(settings);
        command = new GetCourseDetails(mockObserver, mockCourse, factory);
    }

    @Test
    public void testCall() throws Exception {
        verifyZeroInteractions(mockObserver);
        when(factory.getFullCourseInfoTask(mockCourse))
                .thenReturn(
                        new Callable<Course>() {
                            @Override
                            public Course call() throws Exception {
                                return mockCourse;
                            }
                        });
        Course course = command.call();
        assertThat(course).isEqualTo(mockCourse);
    }
}
