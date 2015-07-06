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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.powermock.api.mockito.PowerMockito;
import static org.powermock.api.mockito.PowerMockito.when;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(TmcJsonParser.class)
public class ReviewHandlerTest {

    ReviewHandler handler;
    
    public ReviewHandlerTest() {
    }

    @Before
    public void setUp() throws IOException {
    //    handler = new ReviewHandler();
        PowerMockito.mockStatic(TmcJsonParser.class);
    /*    when(TmcJsonParser.getReviews(anyString()))
                .thenReturn(
                        new ReviewListBuilder()
                                .withExercise(3, true)
                                .withExercise(123, false)
                                .withExercise(52, false)
                                .build()
                );*/
    }

    @After
    public void tearDown() {
    }

    @Test
    public void fetchReviewReturnsEmptyListIfServerSendsNull() throws IOException {
       // when(TmcJsonParser.getReviews(anyString())).thenReturn(null);
        assertNotNull(handler.fetchFromServer(new Course()));
        assertEquals(0, handler.fetchFromServer(new Course()).size());
    }
    
    @Test
    public void reviewsFetchedFromCorrectUrl() throws Exception {
        String url = "www.tmc.mooc.fi.duck/reviews";
        Course course = new Course();
        course.setReviewsUrl(url);
        handler.getNewObjects(course);
        PowerMockito.verifyStatic();
//        TmcJsonParser.getReviews(eq(url));
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
