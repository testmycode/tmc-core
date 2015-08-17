package fi.helsinki.cs.tmc.core.commands;

import static org.junit.Assert.assertEquals;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fi.helsinki.cs.tmc.core.communication.updates.ReviewHandler;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Review;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class GetUnreadReviewsTest {

    private ReviewHandler handler;

    @Before
    public void setUp() {
        handler = mock(ReviewHandler.class);
    }

    @Test
    public void testDelegatesWorkToReviewHandler() throws Exception {
        Course course = new Course();
        List<Review> expected = Arrays.asList(new Review());
        when(handler.getNewObjects(course)).thenReturn(expected);

        List<Review> actual = new GetUnreadReviews(course, handler).call();

        assertEquals(expected, actual);
    }

    @Test(expected = TmcCoreException.class)
    public void testThrowsExceptionOnHandlerException() throws Exception {
        when(handler.getNewObjects(any(Course.class))).thenThrow(new Exception());

        new GetUnreadReviews(new Course(), handler).call();
    }
}
