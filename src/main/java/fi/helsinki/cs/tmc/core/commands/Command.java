package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.exceptions.TmcInterruptionException;

import java.util.concurrent.Callable;

/**
 * A task that can be completed by the TMC-Core.
 *
 * <p>Third parties should use these via {@link fi.helsinki.cs.tmc.core.TmcCore}.
 */
public abstract class Command<E> implements Callable<E> {

    protected TmcSettings settings;
    protected ProgressObserver observer;

    /**
     * Constructs a Command object.
     */
    public Command() { }


    /**
     * Constructs a Command object with an associated {@link TmcSettings}.
     */
    public Command(TmcSettings settings) {
        this();
        this.settings = settings;
    }

    /**
     * Constructs a Command object with an associated {@link TmcSettings} and
     * {@link ProgressObserver}.
     */
    public Command(TmcSettings settings, ProgressObserver observer) {
        this(settings);
        this.observer = observer;
    }

    /**
     * Informs an associated {@link ProgressObserver} about the current status of the command.
     *
     * <p>If no progress observer is assigned, nothing happens.
     */
    protected void informObserver(double percentageDone, String message) {
        if (observer != null) {
            observer.progress(percentageDone, message);
        }
    }

    /**
     * Informs an associated {@link ProgressObserver} about the current status of the command.
     *
     * <p>The provided values are converted into a percentage before passing to the observer.
     *
     * <p>If no progress observer is assigned, nothing happens.
     */
    protected void informObserver(int currentProgress, int maxProgress, String message) {
        double percent = ((double) currentProgress) * 100 / maxProgress;
        informObserver(percent, message);
    }

    protected void checkInterrupt() throws TmcInterruptionException {
        if (Thread.currentThread().isInterrupted()) {
            throw new TmcInterruptionException();
        }
    }
}
