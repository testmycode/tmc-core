package fi.helsinki.cs.tmc.core.communication;

import fi.helsinki.cs.tmc.core.communication.http.HttpTasks;
import fi.helsinki.cs.tmc.core.domain.bandicoot.Crash;

import fi.helsinki.cs.tmc.core.domain.bandicoot.Diagnostics;

import org.apache.commons.lang.math.NumberUtils;

import java.net.URI;
import java.util.concurrent.Callable;

public class TmcBandicootCommunicationTaskFactory {

    // Diagnostics url must end in '/', or the path resolves with client_infos and crashes won't work
    private final URI diagnosticsUrl = URI.create("https://tmc-bandicoot.testmycode.io/");

    /**
     * Sends diagnostics to tmc-bandicoot.
     * @param diagnostics diagnostics
     */
    public Callable<Void> sendDiagnostics(Diagnostics diagnostics) {
        URI uri = getUrl(diagnostics);
        final Callable<String> send = new HttpTasks().postJson(uri.resolve("client_infos"), diagnostics);

        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                send.call();
                return null;
            }
        };
    }

    /**
     * Sends a crash report to tmc-bandicoot.
     * @param crash a crash
     */
    public Callable<Void> sendCrash(Crash crash) {
        final Callable<String> send = new HttpTasks().postJson(diagnosticsUrl.resolve("crashes"), crash);

        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                send.call();
                return null;
            }
        };
    }

    private URI getUrl(Diagnostics diagnostics) {
        return diagnosticsUrl;
    }
}
