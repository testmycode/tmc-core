package fi.helsinki.cs.tmc.core.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fi.helsinki.cs.tmc.core.CoreTestSettings;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;
import fi.helsinki.cs.tmc.core.zipping.ProjectRootFinder;
import fi.helsinki.cs.tmc.langs.domain.NoLanguagePluginFoundException;
import fi.helsinki.cs.tmc.langs.util.TaskExecutorImpl;
import fi.helsinki.cs.tmc.stylerunner.validation.Strategy;
import fi.helsinki.cs.tmc.stylerunner.validation.ValidationError;
import fi.helsinki.cs.tmc.stylerunner.validation.ValidationResult;

import com.google.common.base.Optional;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class RunCheckStyleTest {

    private TaskExecutorImpl tmcLangsMock;

    @Before
    public void setUp() throws NoLanguagePluginFoundException {
        tmcLangsMock = Mockito.mock(TaskExecutorImpl.class);
    }

    @Test
    public void testCommandDelegatesToTmcLangs() throws Exception {
        new RunCheckStyle("somePath", tmcLangsMock).call();

        verify(tmcLangsMock, times(1)).runCheckCodeStyle(eq(Paths.get("somePath")));
    }

    @Test(expected = TmcCoreException.class)
    public void testRunCheckStyleThrowsExceptionOnInvalidPath() throws Exception {
        new RunCheckStyle("nosuch").call();
    }

    @Test
    public void testCommandReturnsWhatTmcLangsReturns() throws Exception {
        ValidationResult expected = new ValidationResultImpl();
        when(tmcLangsMock.runCheckCodeStyle(any(Path.class))).thenReturn(expected);

        ValidationResult result = new RunCheckStyle("somePath", tmcLangsMock).call();

        assertEquals(expected, result);
    }

    private class ValidationResultImpl implements ValidationResult {

        @Override
        public Strategy getStrategy() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Map<File, List<ValidationError>> getValidationErrors() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
