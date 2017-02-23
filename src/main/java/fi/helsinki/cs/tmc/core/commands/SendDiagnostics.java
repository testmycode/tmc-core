package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.TmcBandicootCommunicationTaskFactory;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.domain.bandicoot.Diagnostics;

import com.google.common.annotations.VisibleForTesting;

/**
 * Sends general diagnostics about the client.
 */
public class SendDiagnostics extends Command<Void> {

    private TmcBandicootCommunicationTaskFactory tmcBandicootCommunicationTaskFactory;
    private final Diagnostics diagnostics;

    public SendDiagnostics(ProgressObserver observer) {
        super(observer);
        this.diagnostics = new Diagnostics();
        this.tmcBandicootCommunicationTaskFactory = new TmcBandicootCommunicationTaskFactory();

    }

    @VisibleForTesting
    SendDiagnostics(ProgressObserver observer, TmcBandicootCommunicationTaskFactory factory) {
        this(observer);
        this.tmcBandicootCommunicationTaskFactory = factory;
    }

    /**
     * Sends diagnostics to tmc-bandicoot.
     * @return null
     * @throws Exception ex
     */
    @Override
    public Void call() throws Exception {
        tmcBandicootCommunicationTaskFactory.sendDiagnostics(diagnostics).call();
        return null;
    }
}
