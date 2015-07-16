package hy.tmc.core.commands;

import com.google.common.base.Optional;
import fi.helsinki.cs.tmc.langs.util.TaskExecutorImpl;
import fi.helsinki.cs.tmc.stylerunner.validation.Strategy;
import fi.helsinki.cs.tmc.stylerunner.validation.ValidationError;
import fi.helsinki.cs.tmc.stylerunner.validation.ValidationResult;
import hy.tmc.core.exceptions.TmcCoreException;
import hy.tmc.core.testhelpers.ClientTmcSettings;
import hy.tmc.core.zipping.ProjectRootFinder;
import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.Before;
import static org.mockito.Matchers.eq;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;

public class RunCheckStyleTest {

    private ClientTmcSettings settings;
    private ProjectRootFinder finderMock;
    private TaskExecutorImpl taskExecutorMock;

    @Before
    public void setup() {
        this.settings = new ClientTmcSettings();
        this.settings.setUsername("test");
        this.settings.setPassword("1234");
        this.settings.setServerAddress("https://tmc.mooc.fi/staging");
        this.finderMock = Mockito.mock(ProjectRootFinder.class);
        this.taskExecutorMock = Mockito.mock(TaskExecutorImpl.class);
    }

    private void createAndCheckdata() throws TmcCoreException {
        RunCheckStyle newCheckStyle = new RunCheckStyle("polku", settings);
        newCheckStyle.checkData();
    }

    @Test(expected = TmcCoreException.class)
    public void exceptionIfnoPath() throws Exception {
        RunCheckStyle newCheckStyle = new RunCheckStyle("", settings);
        newCheckStyle.checkData();
    }

    @Test(expected = TmcCoreException.class)
    public void exceptionIfPasswordIsNotPresent() throws Exception {
        this.settings.setPassword("");
        createAndCheckdata();
    }

    @Test(expected = TmcCoreException.class)
    public void exceptionIfUsernameIsNotPresent() throws Exception {
        this.settings.setUsername("");
        createAndCheckdata();
    }

    @Test(expected = TmcCoreException.class)
    public void exceptionIfServerAddressIsNotPresent() throws Exception {
        this.settings.setServerAddress("");
        createAndCheckdata();
    }
    
    @Test
    public void taskExecutorImplIsInvoked() throws Exception{
        String pathToExercise = "polku/tiedostoon";
        String rootDirectory = "polku";
        RunCheckStyle checkStyle = new RunCheckStyle(pathToExercise, settings, finderMock, taskExecutorMock);
        
        when(this.finderMock.getRootDirectory(eq(Paths.get(pathToExercise)))).thenReturn(
                Optional.of(Paths.get(rootDirectory))
        );
        when(this.taskExecutorMock.runCheckCodeStyle(eq(Paths.get(rootDirectory)))).thenReturn(
                new ValidationResult() {
            @Override
            public Strategy getStrategy() {
                return null;
            }

            @Override
            public Map<File, List<ValidationError>> getValidationErrors() {
                return null;
            }
        });
        ValidationResult result = checkStyle.call();
        assertFalse(result == null);
        assertTrue(result.getStrategy() == null);
        assertTrue(result.getValidationErrors() == null);
    }
}
