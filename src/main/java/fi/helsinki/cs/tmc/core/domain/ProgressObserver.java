package fi.helsinki.cs.tmc.core.domain;

/**
 * ProgressObserver (made by UI like tmc-cli or tmc-netbeans etc) observes the status of
 * TmcCore-process. When TmcCore-process has done some progress (zipped some file, downloaded one
 * exercise for example), it informs UI in order that end-user will be informed about the progress.
 */
public abstract class ProgressObserver {

    private static class NullProgressObserver extends ProgressObserver {

        @Override
        public void progress(String progressMessage) {
            // NOP
        }

        @Override
        public void progress(Double percentDone, String progressMessage) {
            // NOP
        }
    }

    public static final ProgressObserver NULL_OBSERVER = new NullProgressObserver();

    /**
     * Tells user-interface that some progress is done.
     */
    public abstract void progress(String progressMessage);

    public abstract void progress(Double percentDone, String progressMessage);
}
