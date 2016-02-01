package fi.helsinki.cs.tmc.core.domain;

/**
 * ProgressObserver (made by UI like tmc-cli or tmc-netbeans etc) observes the status of
 * TmcCore-process. When TmcCore-process has done some progress (zipped some file, downloaded one
 * exercise for example), it informs UI in order that end-user will be informed about the progress.
 */
public interface ProgressObserver {
    /**
     * Tells user-interface that some progress is done.
     */
    void progress(String progressMessage);

    void progress(Double percentDone, String progressMessage);
}
