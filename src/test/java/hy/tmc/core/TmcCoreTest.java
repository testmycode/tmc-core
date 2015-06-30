package hy.tmc.core;

import hy.tmc.core.TmcCore;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.util.concurrent.ListeningExecutorService;
import fi.helsinki.cs.tmc.langs.RunResult;
import fi.helsinki.cs.tmc.langs.TestResult;
import hy.tmc.core.domain.submission.SubmissionResult;
import hy.tmc.core.commands.Authenticate;
import hy.tmc.core.commands.ChooseServer;
import hy.tmc.core.commands.Command;
import hy.tmc.core.commands.DownloadExercises;
import hy.tmc.core.commands.ListCourses;
import hy.tmc.core.commands.ListExercises;
import hy.tmc.core.commands.Logout;
import hy.tmc.core.commands.Paste;
import hy.tmc.core.commands.ReplyToPing;
import hy.tmc.core.commands.RunTests;
import hy.tmc.core.commands.Submit;
import hy.tmc.core.exceptions.ProtocolException;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import org.powermock.core.classloader.annotations.PowerMockIgnore;

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
