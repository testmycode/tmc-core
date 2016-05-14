package fi.helsinki.cs.tmc.core.utilities;

import java.util.concurrent.ScheduledThreadPoolExecutor;

/** Holds TMC's default RequestProcessor. */
public class TmcRequestProcessor {
    // TODO: make it to a holder pattern...
    public static final ScheduledThreadPoolExecutor instance = new ScheduledThreadPoolExecutor(5);
}
