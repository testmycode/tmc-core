package fi.helsinki.cs.tmc.spyware;

import static com.google.common.base.Preconditions.checkArgument;

import fi.helsinki.cs.tmc.core.communication.TmcServerCommunicationTaskFactory;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.exceptions.NotLoggedInException;
import fi.helsinki.cs.tmc.core.holders.TmcSettingsHolder;
import fi.helsinki.cs.tmc.core.utilities.Cooldown;
import fi.helsinki.cs.tmc.core.utilities.SingletonTask;
import fi.helsinki.cs.tmc.core.utilities.TmcRequestProcessor;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Buffers {@link LoggableEvent}s and sends them to the server and/or syncs them to the disk
 * periodically.
 */
public class EventSendBuffer implements EventReceiver {
    private static final Logger log = Logger.getLogger(EventSendBuffer.class.getName());

    public static final long DEFAULT_SEND_INTERVAL = 3 * 60 * 1000;
    public static final long DEFAULT_SAVE_INTERVAL = 1 * 60 * 1000;
    public static final int DEFAULT_MAX_EVENTS = 64 * 1024;
    public static final int DEFAULT_AUTOSEND_THREHSOLD = DEFAULT_MAX_EVENTS / 2;
    public static final int DEFAULT_AUTOSEND_COOLDOWN = 30 * 1000;
    public static final int DEFAULT_MAX_EVENTS_PER_SEND = 500;

    private Random random = new Random();
    private SpywareSettings settings;
    private TmcServerCommunicationTaskFactory serverAccess;
    private EventStore eventStore;

    // The following variables must only be accessed with a lock on sendQueue.
    private final ArrayDeque<LoggableEvent> sendQueue = new ArrayDeque<>();
    private int eventsToRemoveAfterSend = 0;
    private int maxEvents = DEFAULT_MAX_EVENTS;
    private int autosendThreshold = DEFAULT_AUTOSEND_THREHSOLD;
    private int maxEventsPerSend = DEFAULT_MAX_EVENTS_PER_SEND; // Servers have POST size limits
    private Cooldown autosendCooldown;

    public EventSendBuffer(SpywareSettings settings, EventStore eventStore) {
        this(settings, new TmcServerCommunicationTaskFactory(), eventStore);
    }

    public EventSendBuffer(
            SpywareSettings settings,
            TmcServerCommunicationTaskFactory serverAccess,
            EventStore eventStore) {
        this.settings = settings;
        this.serverAccess = serverAccess;
        this.eventStore = eventStore;
        this.autosendCooldown = new Cooldown(DEFAULT_AUTOSEND_COOLDOWN);

        try {
            List<LoggableEvent> initialEvents = Arrays.asList(eventStore.load());
            initialEvents = initialEvents.subList(0, Math.min(maxEvents, initialEvents.size()));
            this.sendQueue.addAll(initialEvents);
        } catch (IOException | RuntimeException ex) {
            log.log(Level.WARNING, "Failed to read events from event store", ex);
        }

        this.sendingTask.setInterval(DEFAULT_SEND_INTERVAL);
        this.savingTask.setInterval(DEFAULT_SAVE_INTERVAL);
    }

    public void setSendingInterval(long interval) {
        checkArgument(interval >= 0);
        this.sendingTask.setInterval(interval);
    }

    public void setSavingInterval(long interval) {
        checkArgument(interval >= 0);
        this.savingTask.setInterval(interval);
    }

    public void setMaxEvents(int newMaxEvents) {
        checkArgument(newMaxEvents > 0);

        synchronized (sendQueue) {
            if (newMaxEvents < maxEvents) {
                int diff = newMaxEvents - maxEvents;
                for (int i = 0; i < diff; ++i) {
                    sendQueue.pop();
                }
                eventsToRemoveAfterSend -= diff;
            }

            maxEvents = newMaxEvents;
        }
    }

    public void setAutosendThreshold(int autosendThreshold) {
        synchronized (sendQueue) {
            if (autosendThreshold <= 0) {
                throw new IllegalArgumentException();
            }
            this.autosendThreshold = autosendThreshold;

            maybeAutosend();
        }
    }

    public void setAutosendCooldown(long durationMillis) {
        checkArgument(durationMillis > 0);
        synchronized (sendQueue) {
            this.autosendCooldown.setDurationMillis(durationMillis);
        }
    }

    public void setMaxEventsPerSend(int maxEventsPerSend) {
        checkArgument(maxEventsPerSend > 0);
        synchronized (sendQueue) {
            this.maxEventsPerSend = maxEventsPerSend;
        }
    }

    public void sendNow() {
        sendingTask.start();
    }

    public void saveNow(long timeout) throws TimeoutException, InterruptedException {
        savingTask.start();
        savingTask.waitUntilFinished(timeout);
    }

    public void waitUntilCurrentSendingFinished(long timeout)
            throws TimeoutException, InterruptedException {
        sendingTask.waitUntilFinished(timeout);
    }

    @Override
    public void receiveEvent(LoggableEvent event) {
        if (!settings.isSpywareEnabled()) {
            return;
        }

        synchronized (sendQueue) {
            if (sendQueue.size() >= maxEvents) {
                sendQueue.pop();
                eventsToRemoveAfterSend--;
            }
            sendQueue.add(event);

            maybeAutosend();
        }
    }

    private void maybeAutosend() {
        if (sendQueue.size() >= autosendThreshold && autosendCooldown.isExpired()) {
            autosendCooldown.start();
            sendNow();
        }
    }

    /**
     * Stops sending any more events.
     *
     * <p>Buffer manipulation methods may still be called.
     */
    @Override
    public void close() {
        long delayPerWait = 2000;
        try {
            sendingTask.unsetInterval();
            savingTask.unsetInterval();

            savingTask.waitUntilFinished(delayPerWait);
            savingTask.start();
            savingTask.waitUntilFinished(delayPerWait);

            sendingTask.waitUntilFinished(delayPerWait);

        } catch (TimeoutException ex) {
            log.log(Level.WARNING, "Time out when closing EventSendBuffer", ex);
        } catch (InterruptedException ex) {
            log.log(Level.WARNING, "Closing EventSendBuffer interrupted", ex);
        }
    }

    private SingletonTask sendingTask =
            new SingletonTask(getSendingTaskRunnable(), TmcRequestProcessor.instance);

    private Runnable getSendingTaskRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                boolean shouldSendMore;

                do {
                    ArrayList<LoggableEvent> eventsToSend = copyEventsToSendFromQueue();
                    if (eventsToSend.isEmpty()) {
                        return;
                    }

                    synchronized (sendQueue) {
                        shouldSendMore = sendQueue.size() > eventsToSend.size();
                    }

                    URI url = pickDestinationUrl();
                    if (url == null) {
                        return;
                    }

                    log.log(
                            Level.INFO,
                            "Sending {0} events to {1}",
                            new Object[] {eventsToSend.size(), url});

                    try {
                        if (!tryToSend(eventsToSend, url)) {
                            shouldSendMore = false;
                        }
                    } catch (NotLoggedInException e) {
                        throw new RuntimeException(
                            "Could not send analytics because not logged in.", e);
                    }
                } while (shouldSendMore);
            }

            private ArrayList<LoggableEvent> copyEventsToSendFromQueue() {
                synchronized (sendQueue) {
                    ArrayList<LoggableEvent> eventsToSend = new ArrayList<>(sendQueue.size());

                    Iterator<LoggableEvent> iterator = sendQueue.iterator();
                    while (iterator.hasNext() && eventsToSend.size() < maxEventsPerSend) {
                        eventsToSend.add(iterator.next());
                    }

                    eventsToRemoveAfterSend = eventsToSend.size();

                    return eventsToSend;
                }
            }

            private URI pickDestinationUrl() {
                Optional<Course> course = TmcSettingsHolder.get().getCurrentCourse();
                if (!course.isPresent()) {
                    log.log(Level.FINE, "Not sending events because no course selected");
                    return null;
                }

                List<URI> urls = course.get().getSpywareUrls();
                if (urls == null || urls.isEmpty()) {
                    log.log(Level.INFO, "Not sending events because no URL provided by server");
                    return null;
                }

                return urls.get(random.nextInt(urls.size()));
            }

            private boolean tryToSend(final ArrayList<LoggableEvent> eventsToSend, final URI url)
                throws NotLoggedInException {
                Callable<Object> task = serverAccess.getSendEventLogJob(url, eventsToSend);

                // TODO: Should we still wrap this into bg task (future)

                try {
                    task.call();
                } catch (Exception ex) {
                    log.log(Level.INFO, "Sending failed", ex);
                    return false;
                }

                log.log(
                        Level.INFO,
                        "Sent {0} events successfully to {1}",
                        new Object[] {eventsToSend.size(), url});

                removeSentEventsFromQueue();

                // If saving fails now (or is already running and fails later)
                // then we may end up sending duplicate events later.
                // This will hopefully be very rare.
                savingTask.start();
                return true;
            }

            private void removeSentEventsFromQueue() {
                synchronized (sendQueue) {
                    assert (eventsToRemoveAfterSend <= sendQueue.size());
                    while (eventsToRemoveAfterSend > 0) {
                        sendQueue.pop();
                        eventsToRemoveAfterSend--;
                    }
                }
            }
        };
    }

    private Runnable eventsToSaveRunnable =
            new Runnable() {
                @Override
                public void run() {
                    try {
                        LoggableEvent[] eventsToSave;
                        synchronized (sendQueue) {
                            eventsToSave = Iterables.toArray(sendQueue, LoggableEvent.class);
                        }
                        eventStore.save(eventsToSave);
                    } catch (IOException ex) {
                        log.log(Level.WARNING, "Failed to save events", ex);
                    }
                }
            };

    private SingletonTask savingTask =
            new SingletonTask(eventsToSaveRunnable, TmcRequestProcessor.instance);
}
