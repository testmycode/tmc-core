package fi.helsinki.cs.tmc.snapshots;

import java.io.Closeable;

/**
 * An object prepared to receive a loggable event (may be called from any thread).
 *
 * <p>These can be organized into a chain to form a pushing pipeline.
 * The chain should be closed back to front so that any flushing happens
 * in the appropriate order.
 */
public interface EventReceiver extends Closeable {
    /**
     * Receives an event. May be called from any thread non-EDT thread.
     */
    void receiveEvent(LoggableEvent event);
}
