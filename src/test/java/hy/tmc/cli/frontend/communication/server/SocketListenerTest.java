package hy.tmc.cli.frontend.communication.server;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.ListenableFuture;
import hy.tmc.cli.frontend.communication.commands.Help;
import hy.tmc.cli.testhelpers.CommandStub;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutionException;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class SocketListenerTest {
    
     ListenableFuture<String> commandResult;
     DataOutputStream output;
     Socket socket;
     SocketListener listener;
     Help helpCommand;
    
    @Before
    @SuppressWarnings("unchecked")
    public void setup() {
        commandResult = mock(ListenableFuture.class);
        output = mock(DataOutputStream.class);
        socket = mock(Socket.class);
        helpCommand = mock(Help.class);
        when(helpCommand.parseData(anyObject())).thenReturn(Optional.of("Return value"));
        listener = new SocketListener(commandResult, output, socket, helpCommand);
    }

    @Test
    public void whenRunIsCalledCommandResultGetIsInvoked() throws InterruptedException, ExecutionException {
       listener.run();
       verify(commandResult).get();
    }
    
    @Test
    public void whenRunIsCalledWithValidCommandFutureValidOutputIsWritten() throws InterruptedException, ExecutionException, IOException {
        when(commandResult.get()).thenReturn("valid output");
        listener = new SocketListener(commandResult, output, socket, new CommandStub());
        listener.run();
        verify(socket).close();
    }
}
