package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;

import java.util.concurrent.Callable;

public abstract class Command<E> implements Callable<E> {

    protected TmcSettings settings;
    protected ProgressObserver observer;

    /**
     * Command can return any type of object. For example SubmissionResult etc.
     */
    public Command() { }

    public Command(TmcSettings settings) {
        this();
        this.settings = settings;
    }

    public Command(ProgressObserver observer, TmcSettings settings) {
        this(settings);
        this.observer = observer;
    }
}
