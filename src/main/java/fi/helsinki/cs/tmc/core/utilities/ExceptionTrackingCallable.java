package fi.helsinki.cs.tmc.core.utilities;

import fi.helsinki.cs.tmc.core.communication.TmcBandicootCommunicationTaskFactory;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.bandicoot.Crash;
import fi.helsinki.cs.tmc.core.exceptions.NotLoggedInException;
import fi.helsinki.cs.tmc.core.holders.TmcSettingsHolder;

import com.google.common.annotations.VisibleForTesting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

public class ExceptionTrackingCallable<T> implements Callable<T> {

    private final Callable<T> command;
    private final TmcBandicootCommunicationTaskFactory tmcBandicootCommunicationTaskFactory;
    private final TmcSettings settings;
    private final Logger logger = LoggerFactory.getLogger(ExceptionTrackingCallable.class);

    public ExceptionTrackingCallable(final Callable<T> command) {
        this.command = command;
        this.tmcBandicootCommunicationTaskFactory = new TmcBandicootCommunicationTaskFactory();
        this.settings = TmcSettingsHolder.get();
    }

    @VisibleForTesting
    public ExceptionTrackingCallable(final Callable<T> command, TmcBandicootCommunicationTaskFactory factory) {
        this.command = command;
        this.tmcBandicootCommunicationTaskFactory = factory;
        this.settings = TmcSettingsHolder.get();
    }

    @Override
    public T call() throws Exception {
        try {
            return command.call();
        } catch (Exception ex) {
            if (settings.getSendDiagnostics() && !(ex instanceof NotLoggedInException)) {
                try {
                    tmcBandicootCommunicationTaskFactory.sendCrash(new Crash(ex)).call();
                } catch (Exception exception) {
                    logger.warn("Couldn't send crash to the server.");
                }
            }
            throw ex;
        }
    }
}
