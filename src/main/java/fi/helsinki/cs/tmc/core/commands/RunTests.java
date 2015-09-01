package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;
import fi.helsinki.cs.tmc.langs.domain.NoLanguagePluginFoundException;
import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.util.TaskExecutor;
import fi.helsinki.cs.tmc.langs.util.TaskExecutorImpl;

import java.nio.file.Paths;

/**
 * A {@link Command} for running test for an exercise.
 */
public class RunTests extends Command<RunResult> {

    private String path;

    /**
     * Constructs a run tests command with {@code settings} for running test on the project
     * located at {@code path}.
     */
    public RunTests(TmcSettings settings, String path) {
        super(settings);
        this.path = path;
    }

    /**
     * Entry point for launching this command.
     */
    @Override
    public RunResult call() throws TmcCoreException {
        TaskExecutor tmcLangs = new TaskExecutorImpl();

        try {
            return tmcLangs.runTests(Paths.get(path));
        } catch (NoLanguagePluginFoundException ex) {
            throw new TmcCoreException("Failed to run tests for project", ex);
        }
    }
}
