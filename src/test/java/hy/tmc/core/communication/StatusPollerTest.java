package hy.tmc.core.communication;

import hy.tmc.core.domain.Course;
import hy.tmc.core.testhelpers.MailExample;
import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;

@RunWith(PowerMockRunner.class)
@PrepareForTest(TmcJsonParser.class)
public class StatusPollerTest {

    private StatusPoller statusPoller;

    @Before
    public void before() throws Exception {
        statusPoller = new StatusPoller(new Course(), new PollScheduler(0, TimeUnit.SECONDS));
        PowerMockito.mockStatic(TmcJsonParser.class);
        PowerMockito.when(TmcJsonParser.getReviews(any(String.class)))
                .thenReturn(MailExample.reviewExample());
    }

    @Test
    public void afterOneIterationThereShouldBeMessagesPolled() throws Exception {
        statusPoller.runOneIteration();
        assertTrue(Mailbox.getMailbox().get().reviewsWaiting());
    }

    @Test(expected = IllegalStateException.class)
    public void failsIfNoMailBoxCreated() throws Exception {
        Mailbox.destroy();
        statusPoller.runOneIteration();
    }

} 
