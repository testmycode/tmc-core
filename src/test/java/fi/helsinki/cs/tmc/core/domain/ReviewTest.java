package fi.helsinki.cs.tmc.core.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import fi.helsinki.cs.tmc.core.communication.HttpResult;
import fi.helsinki.cs.tmc.core.communication.UrlCommunicator;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;

import com.google.common.base.Optional;

import edu.emory.mathcs.backport.java.util.Arrays;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.Test;

import org.mockito.Mockito;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.ArrayList;

@RunWith(PowerMockRunner.class)
@PrepareForTest(UrlCommunicator.class)
public class ReviewTest {

    private static final String updateUrl = "http://test.mooc.duck.fi/courses/47/reviews/8";
    private static final String putUrl = updateUrl + ".json?api_version=7";
    private Review review;

    @Before
    public void setUpReview() {
        review = new Review();
        review.setUpdateUrl(updateUrl);
        review.setId(1);
        review.setCreatedAt("10.6.2015");
        review.setExerciseName("Nimi01");
        review.setMarkedAsRead(false);
        String[] points = new String[] {"1.1", "1.2"};
        String[] pointsNot = new String[] {"1.3", "1.4"};
        review.setPoints(new ArrayList<String>(Arrays.<String>asList(points)));
        review.setPointsNotAwarded(new ArrayList<String>(Arrays.<String>asList(pointsNot)));
        review.setReviewBody("Nice coding!");
        review.setReviewerName("Samu");
        review.setSubmissionId(2);
        review.setUpdatedAt("09.6.2015");
        review.setUrl("http://localhost:8080/url");
    }

    @Test
    public void toStringTest() {
        Review newReview = new Review();
        newReview.setExerciseName("viikko1_tehtava007");
        newReview.setReviewerName("ilari");
        newReview.setReviewBody("ihan hyvä, muista sisennys!");
        String expected = "viikko1_tehtava007 reviewed by ilari:\nihan hyvä, muista sisennys!";
        assertTrue(newReview.toString().contains(expected));
    }

    @Test
    public void markAsReadTest() throws IOException, TmcCoreException {
        UrlCommunicator urlcom = mockUrlCommunicator();
        review.setMarkedAsRead(false);
        review.markAs(true, urlcom);
        assertTrue(review.isMarkedAsRead());
    }

    private UrlCommunicator mockUrlCommunicator() throws IOException {
        UrlCommunicator urlcom = Mockito.mock(UrlCommunicator.class);
        when(urlcom.makePutRequest(Mockito.anyString(), Mockito.any(Optional.class)))
                .thenReturn(new HttpResult("OK", 200, true));
        return urlcom;
    }

    @Test
    public void markAsUnreadTest() throws IOException, TmcCoreException {
        UrlCommunicator urlcom = mockUrlCommunicator();
        review.setMarkedAsRead(true);
        review.markAs(false, urlcom);
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
