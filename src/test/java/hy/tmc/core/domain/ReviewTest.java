package hy.tmc.core.domain;

import hy.tmc.core.domain.Review;
import com.google.common.base.Optional;
import edu.emory.mathcs.backport.java.util.Arrays;
import hy.tmc.core.communication.HttpResult;
import hy.tmc.core.communication.UrlCommunicator;
import hy.tmc.core.exceptions.TmcCoreException;
import java.io.IOException;
import java.util.ArrayList;
import static org.hamcrest.CoreMatchers.not;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(UrlCommunicator.class)
public class ReviewTest {

    private final String updateUrl = "http://test.mooc.duck.fi/courses/47/reviews/8";
    private final String putUrl = this.updateUrl + ".json?api_version=7";
    private Review review;

    @Before
    public void setUp() throws IOException, TmcCoreException {
        PowerMockito.mockStatic(UrlCommunicator.class);
        PowerMockito
                .when(UrlCommunicator.makePutRequest(argThat(not(putUrl)), any(Optional.class)))
                .thenReturn(new HttpResult("", 400, false));
        PowerMockito
                .when(UrlCommunicator.makePutRequest(eq(putUrl), any(Optional.class)))
                .thenReturn(new HttpResult(
                                "{\"status\":OK}", 200, true
                        ));
    }

    @Before
    public void setUpReview() {
        review = new Review();
        review.setUpdateUrl(updateUrl);
        review.setId(1);
        review.setCreatedAt("10.6.2015");
        review.setExerciseName("Nimi01");
        review.setMarkedAsRead(false);
        String[] points = new String[]{"1.1", "1.2"};
        String[] pointsNot = new String[]{"1.3", "1.4"};
        review.setPoints(new ArrayList<String>(Arrays.<String>asList(points)));
        review.setPointsNotAwarded(new ArrayList<String>(Arrays.<String>asList(pointsNot)));
        review.setReviewBody("Nice coding!");
        review.setReviewerName("Samu");
        review.setSubmissionId(2);
        review.setUpdatedAt("09.6.2015");
        review.setUrl("http://localhost:8080/url");
    }

    @After
    public void tearDown() {
    }

    @Test
    public void toStringTest() {
        Review r = new Review();
        r.setExerciseName("viikko1_tehtava007");
        r.setReviewerName("ilari");
        r.setReviewBody("ihan hyvä, muista sisennys!");
        String expected = "viikko1_tehtava007 reviewed by ilari:\nihan hyvä, muista sisennys!";
        assertTrue(r.toString().contains(expected));
    }

    @Test
    public void markAsReadTest() throws IOException, TmcCoreException {
        review.setMarkedAsRead(false);
        review.markAs(true);
        assertTrue(review.isMarkedAsRead());
    }

    @Test
    public void markAsUnreadTest() throws IOException, TmcCoreException {
        review.setMarkedAsRead(true);
        review.markAs(false);
        assertFalse(review.isMarkedAsRead());
    }

    @Test
    public void testGetSubmissionId() {
        assertEquals(2, review.getSubmissionId());
    }

    @Test
    public void testGetExerciseName() {
        assertEquals("Nimi01", review.getExerciseName());
    }

    @Test
    public void testGetId() {
        assertEquals(1, review.getId());
    }

    @Test
    public void testIsMarkedAsRead() {
        assertEquals(false, review.isMarkedAsRead());
    }

    @Test
    public void testGetReviewerName() {
        assertEquals("Samu", review.getReviewerName());
    }

    @Test
    public void testGetReviewBody() {
        assertEquals("Nice coding!", review.getReviewBody());
    }

    @Test
    public void testGetPoints() {
        assertEquals("1.1", review.getPoints().get(0));
    }

    @Test
    public void testGetPointsNotAwarded() {
        assertEquals("1.3", review.getPointsNotAwarded().get(0));
    }

    @Test
    public void testGetUrl() {
        assertEquals("http://localhost:8080/url", review.getUrl());
    }

    @Test
    public void testGetCreatedAt() {
        assertEquals("10.6.2015", review.getCreatedAt());
    }
}
