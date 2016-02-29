package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.TmcServerCommunicationTaskFactory;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.spyware.LoggableEvent;
import fi.helsinki.cs.tmc.core.spyware.NoSpywareServerException;

import java.util.List;
import java.util.Random;

/**
 * A {@link Command} for sending spyware data to the server.
 */
public class SendSpywareEvents extends Command<Void> {

    private Course currentCourse;
    private List<LoggableEvent> events;

    public SendSpywareEvents(
            ProgressObserver observer,
            Course currentCourse,
            List<LoggableEvent> events) {
        super(observer);
        this.currentCourse = currentCourse;
        this.events = events;
    }

    @Override
    public Void call() throws Exception {
        if (currentCourse.getSpywareUrls() == null
                || currentCourse.getSpywareUrls().isEmpty()) {
            throw new NoSpywareServerException("Current course has no spyware servers sets");
        }

        int serverId = new Random().nextInt(currentCourse.getSpywareUrls().size());
        //TODO: Str -> URI
        String spywareServerUri = currentCourse.getSpywareUrls().get(serverId).toString();

        new TmcServerCommunicationTaskFactory()
                .getSendEventLogJob(spywareServerUri, events)
                .call();

        return null; // Can't instantiate Void
    }
}


