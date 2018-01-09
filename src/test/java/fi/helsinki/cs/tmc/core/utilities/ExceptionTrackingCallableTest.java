package fi.helsinki.cs.tmc.core.utilities;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import fi.helsinki.cs.tmc.core.communication.TmcBandicootCommunicationTaskFactory;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.bandicoot.Crash;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;
import fi.helsinki.cs.tmc.core.holders.TmcSettingsHolder;
import fi.helsinki.cs.tmc.core.utils.MockSettings;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.Callable;

public class ExceptionTrackingCallableTest {

    TmcSettings settings;
    @Mock
    TmcBandicootCommunicationTaskFactory factory;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        this.settings = new MockSettings();
        TmcSettingsHolder.set(settings);
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
