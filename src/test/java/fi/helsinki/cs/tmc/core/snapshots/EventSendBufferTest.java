package fi.helsinki.cs.tmc.core.snapshots;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fi.helsinki.cs.tmc.core.communication.TmcServerCommunicationTaskFactory;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.exceptions.NotLoggedInException;
import fi.helsinki.cs.tmc.core.holders.TmcSettingsHolder;
import fi.helsinki.cs.tmc.core.utils.MockSettings;
import fi.helsinki.cs.tmc.snapshots.EventSendBuffer;
import fi.helsinki.cs.tmc.snapshots.EventStore;
import fi.helsinki.cs.tmc.snapshots.LoggableEvent;

import com.google.common.base.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class EventSendBufferTest {

    private Course mockCourse;

    @Spy TmcSettings tmcSettings = new MockSettings();
    @Mock TmcServerCommunicationTaskFactory factory;

    @Mock private EventStore eventStore;

    @Captor private ArgumentCaptor<URI> spywareServerUrl;
    @Captor private ArgumentCaptor<ArrayList<LoggableEvent>> sentEvents;

    private long sendDuration;
    private Semaphore sendStartSemaphore; // released when a send starts
    private Exception sendException;
    private AtomicInteger sendOperationsStarted;
    private AtomicInteger sendOperationsFinished;

    @Captor private ArgumentCaptor<LoggableEvent[]> savedEvents;

    private EventSendBuffer sender;

    @Before
    public void setUp() throws IOException, NotLoggedInException {
        MockitoAnnotations.initMocks(this);

        TmcSettingsHolder.set(tmcSettings);
        mockCourse = new Course("mock-course");
        mockCourse.setSpywareUrls(Arrays.asList(URI.create("http://example.com/")));
        when(tmcSettings.getCurrentCourse()).thenReturn(Optional.of(mockCourse));

        sendDuration = 0;
        sendStartSemaphore = new Semaphore(0);
        sendException = null;
        sendOperationsStarted = new AtomicInteger(0);
        sendOperationsFinished = new AtomicInteger(0);
        when(factory.getSendEventLogJob(spywareServerUrl.capture(), sentEvents.capture()))
                .thenReturn(
                        new Callable<Object>() {
                            @Override
                            public Object call() throws Exception {
                                sendOperationsStarted.incrementAndGet();
                                sendStartSemaphore.release();
                                Thread.sleep(sendDuration);
                                if (sendException != null) {
                                    throw sendException;
                                }
                                sendOperationsFinished.incrementAndGet();
                                return null;
                            }
                        });

        when(eventStore.load()).thenReturn(new LoggableEvent[0]);
        doNothing().when(eventStore).save(savedEvents.capture());

        sender = new EventSendBuffer(factory, eventStore);
    }

    @After
    public void tearDown() {
        sender.close();
    }

    private LoggableEvent mkEvent(int num) {
        return new LoggableEvent("foo" + num, "bar" + num, "baz" + num, new byte[0]);
    }

    private LoggableEvent ev1 = mkEvent(1);
    private LoggableEvent ev2 = mkEvent(2);
    private LoggableEvent ev3 = mkEvent(3);
    private LoggableEvent ev4 = mkEvent(4);
    private LoggableEvent ev5 = mkEvent(5);

    @Test
    public void sendsEventsOnRequest() throws TimeoutException, InterruptedException {
        sender.receiveEvent(ev1);
        sender.receiveEvent(ev2);
        sender.sendNow();

        sender.waitUntilCurrentSendingFinished(1000);
        assertEquals(URI.create("http://example.com/"), spywareServerUrl.getValue());
        assertArrayEquals(new Object[] {ev1, ev2}, sentEvents.getValue().toArray());
    }

    @Test
    public void doesNotSendTooManyEventsAtATime() throws TimeoutException, InterruptedException {
        int wayTooMuch = 10000;
        for (int i = 0; i < wayTooMuch; ++i) {
            sender.receiveEvent(ev1);
        }
        sender.sendNow();

        sender.waitUntilCurrentSendingFinished(1000);

        assertTrue(sentEvents.getAllValues().size() > 1);

        int sum = 0;
        for (ArrayList<LoggableEvent> msg : sentEvents.getAllValues()) {
            assertTrue(msg.size() < wayTooMuch);
            sum += msg.size();
        }
        assertEquals(wayTooMuch, sum);
    }

    @Test
    public void picksServerRandomly() throws TimeoutException, InterruptedException {
        URI[] expected =
                new URI[] {URI.create("http://example1.com/"), URI.create("http://example2.com/")};
        mockCourse.setSpywareUrls(Arrays.asList(expected));

        Set<URI> serversPicked = new HashSet<>();
        for (int i = 0; i < 1000; ++i) {
            sender.receiveEvent(ev1);
            sender.sendNow();
            sender.waitUntilCurrentSendingFinished(1000);
            serversPicked.add(spywareServerUrl.getValue());
            if (serversPicked.size() == expected.length) {
                return;
            }
        }

        fail("Servers picked only contained: " + serversPicked);
    }

    @Test
    public void autosendsPeriodically() throws InterruptedException {
        sender.receiveEvent(ev1);
        sender.setSendingInterval(100);
        Thread.sleep(250);
        sender.receiveEvent(ev2);
        Thread.sleep(250);

        assertTrue(sendOperationsFinished.get() >= 2);
    }

    @Test
    public void autosavesPeriodically() throws InterruptedException, IOException {
        sender.setSendingInterval(10000000);

        sender.receiveEvent(ev1);
        sender.setSavingInterval(100);
        Thread.sleep(250);
        verify(eventStore, atLeast(1)).save(any(LoggableEvent[].class));

        sender.receiveEvent(ev2);
        Thread.sleep(250);

        verify(eventStore, atLeast(2)).save(any(LoggableEvent[].class));

        LoggableEvent[] expecteds = new LoggableEvent[] {ev1, ev2};
        assertArrayEquals(expecteds, savedEvents.getValue());
    }

    @Test
    public void autosendsWhenNumberOfEventsGoesOverThreshold()
            throws TimeoutException, InterruptedException {
        sender.setAutosendThreshold(3);
        sender.receiveEvent(ev1);
        sender.receiveEvent(ev2);
        Thread.sleep(50);
        assertEquals(0, sendOperationsFinished.get());

        sender.receiveEvent(ev3);
        sender.waitUntilCurrentSendingFinished(1000);

        assertEquals(1, sendOperationsFinished.get());
        LoggableEvent[] expecteds = new LoggableEvent[] {ev1, ev2, ev3};
        assertArrayEquals(expecteds, sentEvents.getValue().toArray(new LoggableEvent[0]));
    }

    @Test
    public void autosendingWhenOverThresholdHasACooldown()
            throws TimeoutException, InterruptedException {
        sendException = new RuntimeException("Sending failed");
        sender.setAutosendThreshold(3);
        sender.setAutosendCooldown(30000);
        sender.receiveEvent(ev1);
        sender.receiveEvent(ev2);
        sender.receiveEvent(ev3);
        sender.waitUntilCurrentSendingFinished(1000);
        sender.receiveEvent(ev4);
        sender.waitUntilCurrentSendingFinished(1000);

        assertEquals(1, sendOperationsStarted.get());
        LoggableEvent[] expecteds = new LoggableEvent[] {ev1, ev2, ev3};
        assertArrayEquals(expecteds, sentEvents.getValue().toArray(new LoggableEvent[0]));
    }

    @Test // Issue #125
    public void retryLoopRespectAutosendIntervalOnFailureEvenIfThereIsMoreToSend()
            throws TimeoutException, InterruptedException {
        sendException = new RuntimeException("Sending failed");
        sender.setMaxEventsPerSend(2);
        sender.receiveEvent(ev1);
        sender.receiveEvent(ev2);
        sender.receiveEvent(ev3);
        sender.sendNow();
        sender.waitUntilCurrentSendingFinished(1000);

        assertEquals(1, sendOperationsStarted.get());
        assertEquals(0, sendOperationsFinished.get());
    }

    @Test
    public void discardsOldestEventsOnOverflow() throws TimeoutException, InterruptedException {
        sender.setMaxEvents(3);

        sender.receiveEvent(ev1);
        sender.receiveEvent(ev2);
        sender.receiveEvent(ev3);
        sender.receiveEvent(ev4);
        sender.saveNow(1000);

        LoggableEvent[] expecteds = new LoggableEvent[] {ev2, ev3, ev4};
        assertArrayEquals(expecteds, savedEvents.getValue());

        sender.sendNow();
        sender.waitUntilCurrentSendingFinished(1000);

        assertArrayEquals(expecteds, sentEvents.getValue().toArray(new LoggableEvent[0]));
    }

    @Test
    public void sendsEventsReceivedDuringSendingInSubsequentSend()
            throws TimeoutException, InterruptedException {
        sendDuration = 200;

        sender.receiveEvent(ev1);
        sender.sendNow();
        sendStartSemaphore.acquire();

        sender.receiveEvent(ev2);
        sender.waitUntilCurrentSendingFinished(1000);

        sender.sendNow();
        sender.waitUntilCurrentSendingFinished(1000);

        assertEquals(2, sendOperationsFinished.get());
        assertEquals(2, sentEvents.getAllValues().size());

        assertArrayEquals(
                new LoggableEvent[] {ev1},
                sentEvents.getAllValues().get(0).toArray(new LoggableEvent[0]));
        assertArrayEquals(
                new LoggableEvent[] {ev2},
                sentEvents.getAllValues().get(1).toArray(new LoggableEvent[0]));
    }

    @Test
    public void toleratesOverflowDuringSending() throws TimeoutException, InterruptedException {
        sendDuration = 200;

        sender.setMaxEvents(3);

        sender.receiveEvent(ev1);
        sender.sendNow();
        sendStartSemaphore.acquire();

        sender.receiveEvent(ev2);
        sender.receiveEvent(ev3);
        sender.receiveEvent(ev4);
        sender.receiveEvent(ev5);
        sender.waitUntilCurrentSendingFinished(1000);

        sender.sendNow();
        sender.waitUntilCurrentSendingFinished(1000);

        // Now the queue should be drained and we should be able to send 3 events again
        sender.receiveEvent(ev5);
        sender.receiveEvent(ev4);
        sender.receiveEvent(ev3);
        sender.receiveEvent(ev2);

        sender.sendNow();
        sender.waitUntilCurrentSendingFinished(1000);

        assertEquals(3, sendOperationsFinished.get());
        assertEquals(3, sentEvents.getAllValues().size());

        assertArrayEquals(
                new LoggableEvent[] {ev1},
                sentEvents.getAllValues().get(0).toArray(new LoggableEvent[0]));
        assertArrayEquals(
                new LoggableEvent[] {ev3, ev4, ev5},
                sentEvents.getAllValues().get(1).toArray(new LoggableEvent[0]));
        assertArrayEquals(
                new LoggableEvent[] {ev4, ev3, ev2},
                sentEvents.getAllValues().get(2).toArray(new LoggableEvent[0]));
    }

    @Test
    public void autosavesImmediatelyAfterSend() throws TimeoutException, InterruptedException {
        sender.receiveEvent(ev1);
        sender.sendNow();
        sender.waitUntilCurrentSendingFinished(1000);

        Thread.sleep(100); // Give save task time to fire

        assertEquals(1, savedEvents.getAllValues().size());
    }

    @Test
    public void retainsEventsForNextSendIfSendingFails()
            throws TimeoutException, InterruptedException {
        sendException = new RuntimeException("oh no");
        sender.receiveEvent(ev1);
        sender.sendNow();
        sender.waitUntilCurrentSendingFinished(1000);

        sender.saveNow(1000); // Save explicitly because there's no autosave after a failed send
        assertArrayEquals(new LoggableEvent[] {ev1}, savedEvents.getValue());

        sendException = null;
        sender.sendNow();
        sender.waitUntilCurrentSendingFinished(1000);

        assertEquals(2, sendStartSemaphore.availablePermits());
        assertEquals(1, sendOperationsFinished.get());
        assertArrayEquals(
                new LoggableEvent[] {ev1}, sentEvents.getValue().toArray(new LoggableEvent[0]));

        Thread.sleep(100); // Wait for save
        assertEquals(0, savedEvents.getValue().length);
    }
}
