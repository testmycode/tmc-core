package hy.tmc.core.commands;

import com.google.common.base.Optional;
import fi.helsinki.cs.tmc.langs.domain.NoLanguagePluginFoundException;
import fi.helsinki.cs.tmc.langs.util.TaskExecutorImpl;
import fi.helsinki.cs.tmc.stylerunner.validation.Strategy;
import fi.helsinki.cs.tmc.stylerunner.validation.ValidationError;
import fi.helsinki.cs.tmc.stylerunner.validation.ValidationResult;
import hy.tmc.core.exceptions.TmcCoreException;
import hy.tmc.core.testhelpers.ClientTmcSettings;
import hy.tmc.core.zipping.ProjectRootFinder;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.Before;
import org.mockito.Mockito;




public class RunCheckStyleTest {

    private RunCheckStyle command;
    private ClientTmcSettings settings;
    private TaskExecutorImpl executorMock;
    private ProjectRootFinder finderMock;

    @Before
    public void setUp() throws NoLanguagePluginFoundException {
        executorMock = Mockito.mock(TaskExecutorImpl.class);
        finderMock = Mockito.mock(ProjectRootFinder.class);
        settings = new ClientTmcSettings();
        String path = "/home/peteTheSwede/projects";
        command = new RunCheckStyle(path, settings, finderMock, executorMock);
        when(executorMock.runCheckCodeStyle(any(Path.class))).thenReturn(new ValidationResultImpl());
    }

    @Test
    public void testRunCheckStyle() throws Exception {
        Path path = Paths.get("asdf", "asdf", "qwerty", "kj283u-dkjda93");
        command.runCheckStyle(path);
        verify(executorMock, times(1)).runCheckCodeStyle(eq(path));

    }

    @Test(expected = TmcCoreException.class)
    public void testCheckDataEmptyPath() throws Exception {
        new RunCheckStyle("", settings).checkData();
    }
    
    @Test
    public void testCall() throws Exception {
        Path path = Paths.get("a", "b", "d");
        when(finderMock.getRootDirectory(any(Path.class)))
                .thenReturn(Optional.of(path));
        ValidationResult expected = new ValidationResultImpl();
        when(executorMock.runCheckCodeStyle(eq(path))).thenReturn(expected);
        assertEquals(expected, command.call());
    }

    @Test(expected = TmcCoreException.class)
    public void testThrowsErrorForAbsentExerciseDirectory() throws NoLanguagePluginFoundException, TmcCoreException {
        Path path = Paths.get("a", "b", "d");
        when(finderMock.getRootDirectory(any(Path.class)))
                .thenReturn(Optional.<Path>absent());
        command.call();
    }

    private class ValidationResultImpl implements ValidationResult {

        @Override
        public Strategy getStrategy() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Map<File, List<ValidationError>> getValidationErrors() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }

    private void setupTwo() {
        this.settings = new ClientTmcSettings();
        this.settings.setUsername("test");
        this.settings.setPassword("1234");
        this.settings.setServerAddress("https://tmc.mooc.fi/staging");
        this.finderMock = Mockito.mock(ProjectRootFinder.class);
        this.executorMock = Mockito.mock(TaskExecutorImpl.class);
    }

    private void createAndCheckdata() throws TmcCoreException {
        RunCheckStyle newCheckStyle = new RunCheckStyle("polku", settings);
        newCheckStyle.checkData();
    }

    @Test(expected = TmcCoreException.class)
    public void exceptionIfnoPath() throws Exception {
        setupTwo();
        RunCheckStyle newCheckStyle = new RunCheckStyle("", settings);
        newCheckStyle.checkData();
    }

    @Test(expected = TmcCoreException.class)
    public void exceptionIfPasswordIsNotPresent() throws Exception {
        setupTwo();
        this.settings.setPassword("");
        createAndCheckdata();
    }

    @Test(expected = TmcCoreException.class)
    public void exceptionIfUsernameIsNotPresent() throws Exception {
        setupTwo();
        this.settings.setUsername("");
        createAndCheckdata();
    }

    @Test(expected = TmcCoreException.class)
    public void exceptionIfServerAddressIsNotPresent() throws Exception {
        setupTwo();
        this.settings.setServerAddress("");
        createAndCheckdata();
    }

    @Test
    public void taskExecutorImplIsInvoked() throws Exception {
        setupTwo();
        String pathToExercise = "polku/tiedostoon";
        String rootDirectory = "polku";
        RunCheckStyle checkStyle = new RunCheckStyle(pathToExercise, settings, finderMock, executorMock);

        when(this.finderMock.getRootDirectory(eq(Paths.get(pathToExercise)))).thenReturn(
                Optional.of(Paths.get(rootDirectory))
        );
        when(this.executorMock.runCheckCodeStyle(eq(Paths.get(rootDirectory)))).thenReturn(
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

    @Test(expected = TmcCoreException.class)
    public void exceptionIfPathToExerciseDoesNotExists() throws Exception {
        setupTwo();
        String pathToExercise = "polku/tiedostoon";
        String rootDirectory = "polku";
        RunCheckStyle checkStyle = new RunCheckStyle(pathToExercise, settings, finderMock, executorMock);
        Optional<Path> absentPath = Optional.absent();
        when(this.finderMock.getRootDirectory(eq(Paths.get(pathToExercise)))).thenReturn(
                absentPath
        );
        checkStyle.call();
    }
}
