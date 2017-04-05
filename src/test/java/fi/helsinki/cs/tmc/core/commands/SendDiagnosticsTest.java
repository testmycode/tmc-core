package fi.helsinki.cs.tmc.core.commands;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import fi.helsinki.cs.tmc.core.communication.TmcBandicootCommunicationTaskFactory;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.domain.bandicoot.Diagnostics;
import fi.helsinki.cs.tmc.core.holders.TmcSettingsHolder;
import fi.helsinki.cs.tmc.core.utils.MockSettings;

import org.junit.Before;
import org.junit.Test;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.Callable;

public class SendDiagnosticsTest {

    TmcSettings settings;

    @Mock
    ProgressObserver observer;

    @Mock
    TmcBandicootCommunicationTaskFactory tmcBandicootCommunicationTaskFactory;

    @Captor
    ArgumentCaptor<Diagnostics> diagnosticsCaptor;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.settings = new MockSettings();
        TmcSettingsHolder.set(settings);
        doReturn(new Callable() {
            @Override
            public Object call() throws Exception {
                return null;
            }
        }).when(tmcBandicootCommunicationTaskFactory).sendDiagnostics(any(Diagnostics.class));
    }

    @Test
    public void callsTmcBandicootTaskFactorysSendStatisticsMethod() throws Exception {
        new SendDiagnostics(observer, tmcBandicootCommunicationTaskFactory).call();
        verify(tmcBandicootCommunicationTaskFactory, times(1)).sendDiagnostics(any(Diagnostics.class));
    }
}
