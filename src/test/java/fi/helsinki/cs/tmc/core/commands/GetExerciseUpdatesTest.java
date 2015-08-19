package fi.helsinki.cs.tmc.core.commands;

import static org.junit.Assert.assertEquals;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fi.helsinki.cs.tmc.core.communication.updates.ExerciseUpdateHandler;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class GetExerciseUpdatesTest {

    private ExerciseUpdateHandler handler;

    @Before
    public void setUp() {
        handler = mock(ExerciseUpdateHandler.class);
    }

    @Test
    public void testDelegatesWorkToExerciseUpdateHandler() throws Exception {
        Course course = new Course();
        List<Exercise> expected = Arrays.asList(new Exercise());
        when(handler.getNewObjects(course)).thenReturn(expected);

        List<Exercise> actual = new GetExerciseUpdates(course, handler).call();

        assertEquals(expected, actual);
    }

    @Test(expected = TmcCoreException.class)
    public void testThrowsExceptionOnHandlerException() throws Exception {
        when(handler.getNewObjects(any(Course.class))).thenThrow(new Exception());

        new GetExerciseUpdates(new Course(), handler).call();
    }
}
