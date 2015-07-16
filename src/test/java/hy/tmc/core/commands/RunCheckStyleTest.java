package hy.tmc.core.commands;

import com.google.common.base.Optional;
import fi.helsinki.cs.tmc.langs.domain.NoLanguagePluginFoundException;
import fi.helsinki.cs.tmc.langs.util.TaskExecutorImpl;
import fi.helsinki.cs.tmc.stylerunner.validation.Strategy;
import fi.helsinki.cs.tmc.stylerunner.validation.ValidationError;
import fi.helsinki.cs.tmc.stylerunner.validation.ValidationResult;
import hy.tmc.core.configuration.TmcSettings;
import hy.tmc.core.exceptions.TmcCoreException;
import hy.tmc.core.testhelpers.ClientTmcSettings;
import hy.tmc.core.zipping.ProjectRootFinder;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import org.mockito.Mockito;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RunCheckStyleTest {

    private RunCheckStyle command;
    private TmcSettings settings;
    private TaskExecutorImpl executorMock;
    private ProjectRootFinder finderMock;

    @Before
    public void setUp() throws NoLanguagePluginFoundException {
        executorMock = Mockito.mock(TaskExecutorImpl.class);
        finderMock = Mockito.mock(ProjectRootFinder.class);
        settings = new ClientTmcSettings();
        command = new RunCheckStyle(executorMock, finderMock, settings);
        command.setParameter("path", "/home/peteTheSwede/projects");
        when(executorMock.runCheckCodeStyle(any(Path.class))).thenReturn(new ValidationResultImpl());
    }

    @After
    public void tearDown() {
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

    @Test(expected = TmcCoreException.class)
    public void testCheckDataNoPath() throws Exception {
        new RunCheckStyle(settings).checkData();
    }

    @Test
    public void testCheckDataOk() throws Exception {
        command.checkData();
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
    
    @Test (expected = TmcCoreException.class)
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
}
