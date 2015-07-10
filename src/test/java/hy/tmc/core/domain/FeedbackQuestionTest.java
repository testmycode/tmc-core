
package hy.tmc.core.domain;

import hy.tmc.core.domain.submission.FeedbackQuestion;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;


public class FeedbackQuestionTest {
    
    private FeedbackQuestion question;
    
    public FeedbackQuestionTest() {
    }
    
    @Before
    public void setUp() {
        question = new FeedbackQuestion();
    }
    
    @Test
    public void isIntRangeTest() {
        testWithBounds(0,5);
        testWithBounds(-6,8);
        testWithBounds(123,28199);
        testWithBounds(-48292,-2384);
        testWithBounds(-91023,123);
    }
    
    private void testWithBounds(int min, int max) {
        question.setKind(intrange(min, max));
        assertTrue(question.isIntRange());
        assertFalse(question.isText());
    }
    
    @Test
    public void isTextTest() {
        question.setKind("text");
        assertTrue(question.isText());
        assertFalse(question.isIntRange());
    }
    
    @Test
    public void boundsAreSetCorrectlyTest() {
        assertBoundsAreSet(0,5);
        assertBoundsAreSet(-6,8);
        assertBoundsAreSet(12312,94726);
        assertBoundsAreSet(387,388);
        assertBoundsAreSet(-192,-191);
        assertBoundsAreSet(9,134984);
        assertBoundsAreSet(-2956,5);
        
    }
    
    private void assertBoundsAreSet(int min, int max) {
        question.setKind(intrange(min, max));
        assertEquals(min, question.getIntRangeMin());
        assertEquals(max, question.getIntRangeMax());
    }
    
    @Test (expected = IllegalStateException.class)
    public void wrongBoundThrowException() {
        question.setKind(intrange(4, 3));
    }
    
    @Test (expected = IllegalStateException.class)
    public void wrongBoundThrowException2() {
        question.setKind(intrange(123, -123));
    }
    
    @Test (expected = IllegalStateException.class)
    public void wrongBoundThrowException3() {
        question.setKind(intrange(1239879, 1239878));
    }
    
    private String intrange(int min, int max) {
        return "intrange[" + min + ".." + max + "]";
    }

    
     
}
