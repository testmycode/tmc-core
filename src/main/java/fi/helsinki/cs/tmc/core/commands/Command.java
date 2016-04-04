package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.TmcServerCommunicationTaskFactory;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.exceptions.TmcInterruptionException;
import fi.helsinki.cs.tmc.core.holders.TmcSettingsHolder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/**
 * A task that can be completed by the TMC-Core.
 *
 * <p>Third parties should use these via {@link fi.helsinki.cs.tmc.core.TmcCore}.
 */
public abstract class Command<E> implements Callable<E> {

    private static final Logger logger = LoggerFactory.getLogger(Command.class);

    protected TmcSettings settings;
    protected ProgressObserver observer;

    protected TmcServerCommunicationTaskFactory tmcServerCommunicationTaskFactory;

    /**
     * Constructs a Command object.
     */
    public Command(ProgressObserver observer) {
        this(TmcSettingsHolder.get(), observer);
    }

    /**
     * Constructs a Command object with an associated {@link TmcSettings} and
     * {@link ProgressObserver}.
     */
    public Command(TmcSettings settings, ProgressObserver observer) {
        this(settings, observer, new TmcServerCommunicationTaskFactory());
    }

    public Command(
            ProgressObserver observer,
            TmcServerCommunicationTaskFactory tmcServerCommunicationTaskFactory) {
        this(TmcSettingsHolder.get(), observer, tmcServerCommunicationTaskFactory);
    }

    public Command(
            TmcSettings settings,
            ProgressObserver observer,
            TmcServerCommunicationTaskFactory tmcServerCommunicationTaskFactory) {
        this.settings = settings;
        this.observer = observer;
        this.tmcServerCommunicationTaskFactory = tmcServerCommunicationTaskFactory;
    }

    /**
     * Informs an associated {@link ProgressObserver} about the current status of the command.
     *
     * <p>If no progress observer is assigned, nothing happens.
     */
    protected void informObserver(double percentageDone, String message) {
        observer.progress(percentageDone, message);
    }

    /**
     * Informs an associated {@link ProgressObserver} about the current status of the command.
     *
     * <p>The provided values are converted into a percentage before passing to the observer.
     *
     * <p>If no progress observer is assigned, nothing happens.
     */
    protected void informObserver(int currentProgress, int maxProgress, String message) {
        logger.info(
                "Received notification of "
                        + message
                        + "["
                        + currentProgress
                        + "/"
                        + maxProgress
                        + "]");
        double percent = ((double) currentProgress) * 100 / maxProgress;
        informObserver(percent, message);
    }

    protected void checkInterrupt() throws TmcInterruptionException {
        if (Thread.currentThread().isInterrupted()) {
            logger.info("Noticed interruption, throwing TmcInterruptionException");
            throw new TmcInterruptionException();
        }
    }
}
