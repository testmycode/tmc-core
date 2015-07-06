package hy.tmc.core.updates;

import hy.tmc.core.communication.TmcJsonParser;
import hy.tmc.core.communication.updates.ReviewHandler;
import hy.tmc.core.domain.Course;
import hy.tmc.core.domain.Review;
import hy.tmc.core.testhelpers.builders.ReviewListBuilder;
import java.io.IOException;
import java.util.List;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import org.mockito.Mockito;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ReviewHandlerTest {

    private ReviewHandler handler;
    private TmcJsonParser tmcJsonParser;

    @Before
    public void setUp() throws IOException {
        tmcJsonParser = Mockito.mock(TmcJsonParser.class);
        handler = new ReviewHandler(tmcJsonParser);
        Mockito.when(tmcJsonParser.getReviews(anyString()))
                .thenReturn(
                        new ReviewListBuilder()
                                .withExercise(3, true)
                                .withExercise(123, false)
                                .withExercise(52, false)
                                .build()
                );
    }

    @After
    public void tearDown() {
    }

    @Test
    public void fetchReviewReturnsEmptyListIfServerSendsNull() throws IOException {
        Mockito.when(tmcJsonParser.getReviews(anyString())).thenReturn(null);
        assertNotNull(handler.fetchFromServer(new Course()));
        assertEquals(0, handler.fetchFromServer(new Course()).size());
    }
    
    @Test
    public void reviewsFetchedFromCorrectUrl() throws Exception {
        String url = "www.tmc.mooc.fi.duck/reviews";
        Course course = new Course();
        course.setReviewsUrl(url);
        handler.getNewObjects(course);
        tmcJsonParser.getReviews(eq(url));
    }
    
    @Test
    public void returnsOnlyUnreadReviews() throws Exception {
        List<Review> reviews = handler.getNewObjects(new Course());
        assertTrue(listHasReview(reviews, 123));
        assertTrue(listHasReview(reviews, 52));
        assertFalse(listHasReview(reviews, 3));
    }

    private boolean listHasReview(List<Review> reviews, int id) {
        for (Review review : reviews) {
            if (review.getId() == id) {
                return true;
            }
        }
        return false;
    }
}
