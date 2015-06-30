package hy.tmc.cli.frontend.communication.server;

import hy.tmc.cli.backend.TmcCore;
import hy.tmc.cli.configuration.ConfigHandler;
import hy.tmc.cli.testhelpers.TestClient;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;

public class ServerTest {

    Server server;
    TmcCore tmcCore;
    ExecutorService socketThreadPool;
    TestClient client;
    Thread serverThread;

    @Before
    public void setup() throws IOException {
        tmcCore = mock(TmcCore.class);

        socketThreadPool = Mockito.mock(ExecutorService.class);
        server = new Server(tmcCore, socketThreadPool);

        serverThread = new Thread(server);
        serverThread.start();
        client = new TestClient(server.getCurrentPort());
    }

    private void sendMessageToServer(TestClient client, String message) {
        try {
            client.sendMessage(message);
            Thread.sleep(300);
        } catch (IOException | InterruptedException e) {
            fail("Failed to send message to server or sleep interrupted");
        }
    }

    @After
    public void after() throws IOException {
        server.close();
        serverThread.interrupt();
    }

    @Test
    public void poolIsGivenTasksEvenIfCommandIsBad() throws ProtocolException {
        sendMessageToServer(client, "samu");
        verify(socketThreadPool).submit(Mockito.any(SocketRunnable.class));
    }

    @Test
    public void socketPoolIsGivenATaskWhenACommandIsSent() {
        sendMessageToServer(client, "help");
        verify(socketThreadPool).submit(Mockito.any(SocketRunnable.class));
    }

    @Test
    public void twoClientsSendMessagesAndBothAreIntercepted() throws IOException {
        TestClient clientB = new TestClient(server.getCurrentPort());
        sendMessageToServer(client, "help");
        sendMessageToServer(clientB, "help");
        verify(socketThreadPool, times(2)).submit(any(SocketRunnable.class));
    }

    @Test
    public void twoClientsSendMessagesAndOneIsBadAndBothAreIntercepted() throws IOException {
        TestClient clientB = new TestClient(server.getCurrentPort());
        sendMessageToServer(client, "help");
        sendMessageToServer(clientB, "lol");
        verify(socketThreadPool, times(2)).submit(any(SocketRunnable.class));
    }

    @Test
    public void testGetCurrentPort() {
        int result = server.getCurrentPort();
        assertEquals(new ConfigHandler().readPort(), result);
    }
  
}