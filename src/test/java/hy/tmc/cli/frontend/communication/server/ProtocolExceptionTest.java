package hy.tmc.cli.frontend.communication.server;

import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ProtocolExceptionTest {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test()
    public void afterThrowingMessageCanBeRetrieved() throws ProtocolException {
        expectedEx.expect(ProtocolException.class);
        expectedEx.expectMessage("asd");
        throw new ProtocolException("asd");
    }

    @Test
    public void afterThrowingClauseItCanBeRetrieved() throws ProtocolException {
        expectedEx.expect(ProtocolException.class);
        expectedEx.expectMessage("Throw me");
        String tMsg = "Throw me";
        Throwable thr = new Throwable(tMsg);
        throw new ProtocolException(thr);
    }

}
