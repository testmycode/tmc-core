package fi.helsinki.cs.tmc.core.utilities;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import fi.helsinki.cs.tmc.core.communication.TmcBandicootCommunicationTaskFactory;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.bandicoot.Crash;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;
import fi.helsinki.cs.tmc.core.holders.TmcSettingsHolder;

import org.junit.Before;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Locale;
import java.util.concurrent.Callable;

public class ExceptionTrackingCallableTest {

    @Mock
    TmcSettings settings;
    @Mock
    TmcBandicootCommunicationTaskFactory factory;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        TmcSettingsHolder.set(settings);
        when(settings.getServerAddress()).thenReturn("testAddress");
        when(settings.clientName()).thenReturn("testClient");
        when(settings.clientVersion()).thenReturn("testVersion");
        when(settings.hostProgramName()).thenReturn("testHostProgram");
        when(settings.hostProgramVersion()).thenReturn("testHostProgramVersion");
        when(settings.getLocale()).thenReturn(new Locale("en"));
        when(settings.getSendDiagnostics()).thenReturn(true);
        when(factory.sendCrash(any(Crash.class))).thenReturn(new Callable<Void>() {
            @Override public Void call() throws Exception {
                return null;
            }
        });
    }


    @Test(expected = TmcCoreException.class)
    public void testCall() throws Exception {
        new ExceptionTrackingCallable<>(new ExceptionThrowingCallable<Void>(), factory).call();
    }

    class ExceptionThrowingCallable<T> implements Callable<T> {

        @Override
        public T call() throws Exception {
            Throwable rootEx = new Exception("Root cause");
            Throwable ex = new Exception("Test cause.", rootEx);
            throw new TmcCoreException("Test exception", ex);
        }
    }
}
