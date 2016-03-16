package fi.helsinki.cs.tmc.core.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReviewTest {

    private Review review;

    @Before
    public void setUp() throws URISyntaxException {
        review = new Review();
        review.setSubmissionId(1);
        review.setExerciseName("Exercise");
        review.setId(2);
        review.setMarkedAsRead(false);
        review.setReviewerName("Reviewer");
        review.setReviewBody("BodyText");
        review.setPoints(Collections.<String>emptyList());
        review.setPointsNotAwarded(Collections.<String>emptyList());
        review.setUrl(new URI("http://example.com/url"));
        review.setUpdateUrl(new URI("http://example.com/updateUrl"));
        review.setCreatedAt("CreatedAt");
        review.setUpdatedAt("UpdatedAt");
    }

    @Test
    public void testSubmissionId() {
        assertEquals(1, review.getSubmissionId());
        review.setSubmissionId(2);
        assertEquals(2, review.getSubmissionId());
    }

    @Test
    public void testExerciseName() {
        assertEquals("Exercise", review.getExerciseName());
        review.setExerciseName("Other");
        assertEquals("Other", review.getExerciseName());
    }

    @Test
    public void testId() {
        assertEquals(2, review.getId());
        review.setId(3);
        assertEquals(3, review.getId());
    }

    @Test
    public void testMarkedAsRead() {
        assertFalse(review.isMarkedAsRead());
        review.setMarkedAsRead(true);
        assertTrue(review.isMarkedAsRead());
    }

    @Test
    public void testReviewerName() {
        assertEquals("Reviewer", review.getReviewerName());
        review.setReviewerName("Other");
        assertEquals("Other", review.getReviewerName());
    }

    @Test
    public void testReviewBody() {
        assertEquals("BodyText", review.getReviewBody());
        review.setReviewBody("Other");
        assertEquals("Other", review.getReviewBody());
    }

    @Test
    public void testPoints() {
        assertEquals(Collections.<String>emptyList(), review.getPoints());
        List<String> newList = new ArrayList<>();
        review.setPoints(newList);
        assertEquals(newList, review.getPoints());
    }

    @Test
    public void testPointsNotAwarded() {
        assertEquals(Collections.<String>emptyList(), review.getPointsNotAwarded());
        List<String> newList = new ArrayList<>();
        review.setPointsNotAwarded(newList);
        assertEquals(newList, review.getPointsNotAwarded());
    }

    @Test
    public void testUrl() throws URISyntaxException {
        assertEquals(new URI("http://example.com/url"), review.getUrl());
        review.setUrl(new URI("http://example.com/other"));
        assertEquals(new URI("http://example.com/other"), review.getUrl());
    }

    @Test
    public void testUpdateUrl() throws URISyntaxException {
        assertEquals(new URI("http://example.com/updateUrl"), review.getUpdateUrl());
        review.setUpdateUrl(new URI("http://example.com/other"));
        assertEquals(new URI("http://example.com/other"), review.getUpdateUrl());
    }

    @Test
    public void testCreatedAt() {
        assertEquals("CreatedAt", review.getCreatedAt());
        review.setCreatedAt("Other");
        assertEquals("Other", review.getCreatedAt());
    }

    @Test
    public void testUpdatedAt() {
        assertEquals("UpdatedAt", review.getUpdatedAt());
        review.setUpdatedAt("Other");
        assertEquals("Other", review.getUpdatedAt());
    }

    @Test
    public void testToString() {
        assertEquals(
                "Exercise reviewed by Reviewer:\nBodyText\nhttp://example.com/url",
                review.toString());
    }
}
