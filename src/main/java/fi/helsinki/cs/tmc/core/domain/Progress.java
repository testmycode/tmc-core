package fi.helsinki.cs.tmc.core.domain;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Keeps track of the progress, used for communicating the process for
 * {@link fi.helsinki.cs.tmc.core.domain.ProgressObserver}.
 */
public class Progress {
    AtomicInteger progress = new AtomicInteger();
    double max;

    public Progress(double max) {
        this.max = max;
    }

    public synchronized double incrementAndGet() {
        return progress.incrementAndGet() / max;
    }
}

