package hy.tmc.core;


import com.google.common.util.concurrent.ListeningExecutorService;
import hy.tmc.core.commands.Authenticate;
import hy.tmc.core.commands.ChooseServer;
import hy.tmc.core.commands.DownloadExercises;
import hy.tmc.core.commands.ListCourses;
import hy.tmc.core.commands.ListExercises;
import hy.tmc.core.commands.Logout;
import hy.tmc.core.commands.RunTests;
import hy.tmc.core.commands.Submit;
import hy.tmc.core.exceptions.TmcCoreException;
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
    public void login() throws TmcCoreException, InterruptedException, ExecutionException, Exception {
        tmcCore.login("test", "1234");
        verify(threadPool, times(1)).submit(any(Authenticate.class));
    }

    @Test(expected = TmcCoreException.class)
    public void loginWithoutNumberFails() throws TmcCoreException, InterruptedException, ExecutionException, Exception {
        tmcCore.login("", "").get();
    }

    @Test
    public void logout() throws TmcCoreException, InterruptedException, ExecutionException, Exception {
        tmcCore.logout();
        verify(threadPool, times(1)).submit(any(Logout.class));
    }

    @Test
    public void selectServer() throws TmcCoreException, InterruptedException, ExecutionException, Exception {
        tmcCore.selectServer("uusiServu");
        verify(threadPool, times(1)).submit(any(ChooseServer.class));
    }
    
    
    @Test
    public void downloadExercises() throws TmcCoreException, InterruptedException, ExecutionException, Exception {
        tmcCore.downloadExercises("/polku/tiedostoille", "21");
        verify(threadPool, times(1)).submit(any(DownloadExercises.class));
    }


    @Test
    public void listCourses() throws TmcCoreException, InterruptedException, ExecutionException, Exception {
        tmcCore.listCourses();
        verify(threadPool, times(1)).submit(any(ListCourses.class));  
    }

    @Test
    public void listExercises() throws TmcCoreException, InterruptedException, ExecutionException, Exception {
        tmcCore.listExercises("path/kurssiin");
        verify(threadPool, times(1)).submit(any(ListExercises.class));  
    }

    @Test
    public void test() throws TmcCoreException, InterruptedException, ExecutionException, Exception {
        tmcCore.test("testi/polku");
        verify(threadPool, times(1)).submit(any(RunTests.class));  
    }

    @Test
    public void submit() throws Exception {
        tmcCore.submit("polku/tiedostoon");
        verify(threadPool, times(1)).submit(any(Submit.class));  
    }
    
    @Test(expected=TmcCoreException.class)
    public void submitWithBadPathThrowsException() throws TmcCoreException{
        tmcCore.submit("");
    }
    
    @Test(expected=TmcCoreException.class)
    public void downloadExercisesWithBadPathThrowsException() throws TmcCoreException{
        tmcCore.downloadExercises(null, "2");
    }
    
}
