package fi.helsinki.cs.tmc.core.domain;

import com.google.common.annotations.Beta;

/**
 * ProgressObserver (made by UI like tmc-cli or tmc-netbeans etc) observes the status of
 * TmcCore-process. When TmcCore-process has done some progress (zipped some file, downloaded one
 * exercise for example), it informs UI in order that end-user will be informed about the progress.
 */
public abstract class ProgressObserver {

    private static class NullProgressObserver extends ProgressObserver {

        @Override
        public void progress(long id, String progressMessage) {
            // NOP
        }

        @Override
        public void progress(long id, Double percentDone, String progressMessage) {
            // NOP
        }

        @Override
        public void start(long id) {
            // NOP
        }

        @Override
        public void end(long id) {
            // NOP
        }
    }

    public static final ProgressObserver NULL_OBSERVER = new NullProgressObserver();

    /**
     * Tells user-interface that some progress is done.
     */
    public abstract void progress(long id, String progressMessage);

    public abstract void progress(long id, Double percentDone, String progressMessage);

    /**
     * Tells a static progress observer to start showing the message.
     *
     * @param id id of the stared event
     */
    @Beta
    public abstract void start(long id);

    /**
     * Tells a static progress observer to stop showing the message.
     *
     * @param id id of the stared event
     */
    @Beta
    public abstract void end(long id);
}
