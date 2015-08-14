package fi.helsinki.cs.tmc.core.updates;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import static org.mockito.Matchers.anyString;

import fi.helsinki.cs.tmc.core.communication.TmcApi;
import fi.helsinki.cs.tmc.core.communication.updates.ReviewHandler;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Review;
import fi.helsinki.cs.tmc.core.testhelpers.builders.ReviewListBuilder;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import java.io.IOException;
import java.util.List;

public class ReviewHandlerTest {

    private ReviewHandler handler;
    private TmcApi tmcApi;

    @Before
    public void setUp() throws IOException {

        tmcApi = Mockito.mock(TmcApi.class);
        handler = new ReviewHandler(tmcApi);
        Mockito.when(tmcApi.getReviews(anyString()))
                .thenReturn(
                        new ReviewListBuilder()
                                .withExercise(3, true)
                                .withExercise(123, false)
                                .withExercise(52, false)
                                .build());
    }

    @Test
    public void fetchReviewReturnsEmptyListIfServerSendsNull() throws IOException {

        Mockito.when(tmcApi.getReviews(anyString())).thenReturn(null);
        assertNotNull(handler.fetchFromServer(new Course()));
        assertEquals(0, handler.fetchFromServer(new Course()).size());
    }

    @Test
    public void reviewsFetchedFromCorrectUrl() throws Exception {
        String url = "www.tmc.mooc.fi.duck/reviews";
        Course course = new Course();
        course.setReviewsUrl(url);
        handler.getNewObjects(course);
        tmcApi.getReviews(Mockito.eq(url));
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
