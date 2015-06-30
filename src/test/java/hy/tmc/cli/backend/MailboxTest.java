package hy.tmc.cli.backend;

import hy.tmc.cli.domain.Review;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class MailboxTest {

    private Mailbox mail;

    @Before
    public void setup() {
        Mailbox.create();
        mail = Mailbox.getMailbox().get();
    }

    @Test
    public void whenFilledThereAreNewReviews() {
        assertFalse(mail.reviewsWaiting());
        mail.fill(reviews("nice job", "lol", "hello world"));
        assertTrue(mail.reviewsWaiting());
    }

    @Test
    public void filledReviewsAreWhatTheyShouldBe() {
        mail.fill(reviews("nice job", "lol", "hello world"));
        boolean helloFound = false;
        boolean lelFound = false;
        for (Review r : mail.getUnreadReviews()) {
            if (r.getReviewBody().equals("hello world")) {
                helloFound = true;
            }
            if (r.getReviewBody().equals("lel")) {
                lelFound = true;
            }
        }
        assertTrue(helloFound);
        assertFalse(lelFound);
    }

    @Test
    public void afterReadingReviewsTheMailboxIsEmptyOfReviews() {
        mail.fill(reviews("nice job", "lol", "hello world"));
        assertTrue(mail.reviewsWaiting());
        mail.getUnreadReviews();
        assertFalse(mail.reviewsWaiting());
    }

    @Test
    public void afterFillingTwiceGetReturnsAll() {
        mail.fill(reviews("first", "second"));
        mail.fill(reviews("third", "duck", "fifth"));
        List<Review> reviews = mail.getUnreadReviews();
        assertEquals(5, reviews.size());
        assertEquals("first", reviews.get(0).getReviewBody());
        assertEquals("second", reviews.get(1).getReviewBody());
        assertEquals("third", reviews.get(2).getReviewBody());
        assertEquals("duck", reviews.get(3).getReviewBody());
        assertEquals("fifth", reviews.get(4).getReviewBody());
    }

    @Test (expected = IllegalStateException.class)
    public void fillingWithoutMailboxThrowsError() {
        Mailbox mailbox = Mailbox.getMailbox().get();
        Mailbox.destroy();
        mailbox.fill(new ArrayList<Review>());
    }

    private List<Review> reviews(String... reviewMsgs) {
        List<Review> reviews = new ArrayList<>();
        for (String review : reviewMsgs) {
            Review r = new Review();
            r.setReviewBody(review);
            reviews.add(r);
        }
        return reviews;
    }

}
