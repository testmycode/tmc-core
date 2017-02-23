package fi.helsinki.cs.tmc.core.utilities;

import fi.helsinki.cs.tmc.core.communication.TmcBandicootCommunicationTaskFactory;
import fi.helsinki.cs.tmc.core.domain.bandicoot.Crash;

import java.util.concurrent.Callable;

public class ExceptionTrackingCallable<T> implements Callable<T> {

    private final Callable<T> command;
    private final TmcBandicootCommunicationTaskFactory tmcBandicootCommunicationTaskFactory;

    public ExceptionTrackingCallable(final Callable<T> command) {
        this.command = command;
        this.tmcBandicootCommunicationTaskFactory = new TmcBandicootCommunicationTaskFactory();
    }

    @Override
    public T call() throws Exception {
        try {
            return command.call();
        } catch (Exception ex) {
            tmcBandicootCommunicationTaskFactory.sendCrash(new Crash(ex)).call();
            throw ex;
        }
    }
}
