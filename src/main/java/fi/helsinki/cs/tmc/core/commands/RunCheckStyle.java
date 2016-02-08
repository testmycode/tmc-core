package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;
import fi.helsinki.cs.tmc.langs.abstraction.ValidationResult;
import fi.helsinki.cs.tmc.langs.domain.NoLanguagePluginFoundException;
import fi.helsinki.cs.tmc.langs.util.TaskExecutorImpl;

import java.nio.file.Path;

/**
 * A {@link Command} for running code style validations on an exercise.
 */
public class RunCheckStyle extends Command<ValidationResult> {

    private TaskExecutorImpl tmcLangs;
    private Path path;
    private TmcSettings settings;

    /**
     * Constructs a new run check style command for running a code style check using
     * {@code tmcLangs} on the project at {@code path}.
     */
    public RunCheckStyle(Path path, TaskExecutorImpl tmcLangs, TmcSettings settings) {
        this.path = path;
        this.tmcLangs = tmcLangs;
        this.settings = settings;
    }

    /**
     * Constructs a new run check style command with {@code settings} for running a code style check
     * on the project at {@code path}.
     */
    public RunCheckStyle(Path path, TmcSettings settings) {
        this(path, new TaskExecutorImpl(), settings);
    }

    /**
     * Entry point for launching this command.
     */
    @Override
    public ValidationResult call() throws TmcCoreException {
        try {
            return tmcLangs.runCheckCodeStyle(path, settings.getLocale());
        } catch (NoLanguagePluginFoundException ex) {
            throw new TmcCoreException("Unable to run code style validations on target path", ex);
        }
    }
}
