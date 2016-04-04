package fi.helsinki.cs.tmc.core.commands;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import fi.helsinki.cs.tmc.core.communication.TmcServerCommunicationTaskFactory;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.holders.TmcSettingsHolder;

import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.concurrent.Callable;

public class ListCoursesTest {

    @Mock ProgressObserver mockObserver;
    @Mock TmcSettings settings;
    @Mock TmcServerCommunicationTaskFactory factory;
    @Mock Course courseOne;
    @Mock Course courseTwo;

    private Command<List<Course>> command;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        TmcSettingsHolder.set(settings);
        command = new ListCourses(mockObserver, factory);
    }

    @Test
    public void testCall() throws Exception {
        verifyZeroInteractions(mockObserver);
        when(factory.getDownloadingCourseListTask())
                .thenReturn(
                        new Callable<List<Course>>() {
                            @Override
                            public List<Course> call() throws Exception {
                                return ImmutableList.of(courseOne, courseTwo);
                            }
                        });
        List<Course> courses = command.call();
        assertThat(courses).containsAllOf(courseOne, courseTwo).inOrder();
    }
}
