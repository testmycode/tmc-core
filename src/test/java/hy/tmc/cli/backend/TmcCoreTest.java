package hy.tmc.cli.backend;

import com.google.common.util.concurrent.ListeningExecutorService;
import hy.tmc.cli.frontend.communication.commands.Authenticate;
import hy.tmc.cli.frontend.communication.commands.ChooseServer;
import hy.tmc.cli.frontend.communication.commands.DownloadExercises;
import hy.tmc.cli.frontend.communication.commands.ListCourses;
import hy.tmc.cli.frontend.communication.commands.ListExercises;
import hy.tmc.cli.frontend.communication.commands.Logout;
import hy.tmc.cli.frontend.communication.commands.RunTests;
import hy.tmc.cli.frontend.communication.commands.Submit;
import hy.tmc.core.exceptions.ProtocolException;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.concurrent.ExecutionException;
import org.junit.Test;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;

@RunWith(PowerMockRunner.class)
public class TmcCoreTest {

    private TmcCore tmcCore;
    private ListeningExecutorService threadPool;

    @Before
    public void setUp() {
        threadPool = mock(ListeningExecutorService.class);
        tmcCore = new TmcCore(threadPool);
    }

    @Test
    public void login() throws ProtocolException, InterruptedException, ExecutionException, Exception {
        tmcCore.login("test", "1234");
        verify(threadPool, times(1)).submit(any(Authenticate.class));
    }

    @Test(expected = ProtocolException.class)
    public void loginWithoutNumberFails() throws ProtocolException, InterruptedException, ExecutionException, Exception {
        tmcCore.login("", "").get();
    }

    @Test
    public void logout() throws ProtocolException, InterruptedException, ExecutionException, Exception {
        tmcCore.logout();
        verify(threadPool, times(1)).submit(any(Logout.class));
    }

    @Test
    public void selectServer() throws ProtocolException, InterruptedException, ExecutionException, Exception {
        tmcCore.selectServer("uusiServu");
        verify(threadPool, times(1)).submit(any(ChooseServer.class));
    }
    
    
    @Test
    public void downloadExercises() throws ProtocolException, InterruptedException, ExecutionException, Exception {
        tmcCore.downloadExercises("/polku/tiedostoille", "21");
        verify(threadPool, times(1)).submit(any(DownloadExercises.class));
    }


    @Test
    public void listCourses() throws ProtocolException, InterruptedException, ExecutionException, Exception {
        tmcCore.listCourses();
        verify(threadPool, times(1)).submit(any(ListCourses.class));  
    }

    @Test
    public void listExercises() throws ProtocolException, InterruptedException, ExecutionException, Exception {
        tmcCore.listExercises("path/kurssiin");
        verify(threadPool, times(1)).submit(any(ListExercises.class));  
    }

    @Test
    public void test() throws ProtocolException, InterruptedException, ExecutionException, Exception {
        tmcCore.test("testi/polku");
        verify(threadPool, times(1)).submit(any(RunTests.class));  
    }

    @Test
    public void submit() throws Exception {
        tmcCore.submit("polku/tiedostoon");
        verify(threadPool, times(1)).submit(any(Submit.class));  
    }
    
    @Test(expected=ProtocolException.class)
    public void submitWithBadPathThrowsException() throws ProtocolException{
        tmcCore.submit("");
    }
    
    @Test(expected=ProtocolException.class)
    public void downloadExercisesWithBadPathThrowsException() throws ProtocolException{
        tmcCore.downloadExercises(null, "2");
    }
    
}
