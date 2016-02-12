package fi.helsinki.cs.tmc.core.commands;

import static org.junit.Assert.assertEquals;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fi.helsinki.cs.tmc.core.CoreTestSettings;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;
import fi.helsinki.cs.tmc.langs.abstraction.Strategy;
import fi.helsinki.cs.tmc.langs.abstraction.ValidationError;
import fi.helsinki.cs.tmc.langs.abstraction.ValidationResult;
import fi.helsinki.cs.tmc.langs.domain.NoLanguagePluginFoundException;
import fi.helsinki.cs.tmc.langs.util.TaskExecutorImpl;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RunCheckStyleTest {

    private TaskExecutorImpl tmcLangsMock;
    private CoreTestSettings settings;

    @Before
    public void setUp() throws NoLanguagePluginFoundException {
        tmcLangsMock = Mockito.mock(TaskExecutorImpl.class);
        settings = new CoreTestSettings();
        settings.setLocale(Locale.ENGLISH);
    }

    @Test
    public void testCommandDelegatesToTmcLangs() throws Exception {
        new RunCheckStyle(Paths.get("somePath"), tmcLangsMock, settings).call();

        verify(tmcLangsMock).runCheckCodeStyle(eq(Paths.get("somePath")), eq(Locale.ENGLISH));
    }

    @Test(expected = TmcCoreException.class)
    public void testRunCheckStyleThrowsExceptionOnInvalidPath() throws Exception {
        new RunCheckStyle(Paths.get("nosuch"), settings).call();
    }

    @Test
    public void testCommandReturnsWhatTmcLangsReturns() throws Exception {
        ValidationResult expected = new ValidationResultImpl();
        when(tmcLangsMock.runCheckCodeStyle(any(Path.class), eq(Locale.ENGLISH)))
                .thenReturn(expected);

        ValidationResult result = new RunCheckStyle(Paths.get("somePath"), tmcLangsMock, settings)
                .call();

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
