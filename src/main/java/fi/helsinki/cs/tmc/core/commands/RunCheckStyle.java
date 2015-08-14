package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.TmcApi;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;
import fi.helsinki.cs.tmc.core.zipping.ProjectRootFinder;
import fi.helsinki.cs.tmc.langs.domain.NoLanguagePluginFoundException;
import fi.helsinki.cs.tmc.langs.util.TaskExecutorImpl;
import fi.helsinki.cs.tmc.stylerunner.validation.ValidationResult;

import com.google.common.base.Optional;

import java.nio.file.Path;
import java.nio.file.Paths;

public class RunCheckStyle extends Command<ValidationResult> {

    private ProjectRootFinder finder;
    private TaskExecutorImpl taskExecutor;
    private String path;

    /**
     * Default constructor.
     * @param path to exercise, that should be passed to TmcLangs checkstyle-runner.
     * @param settings containing at least credentials and serverAddress to
     *     enable communication with server.
     */
    public RunCheckStyle(String path, TmcSettings settings) {
        this(
                path,
                settings,
                new ProjectRootFinder(new TaskExecutorImpl(), new TmcApi(settings)),
                new TaskExecutorImpl());
    }

    /**
     * Constructor for dependency injection in tests.
     */
    public RunCheckStyle(
            String path,
            TmcSettings settings,
            ProjectRootFinder finder,
            TaskExecutorImpl executor) {
        super(settings);
        this.path = path;
        this.finder = finder;
        this.taskExecutor = executor;
    }

    @Override
    public ValidationResult call() throws TmcCoreException, NoLanguagePluginFoundException {
        Optional<Path> rootDirectory = finder.getRootDirectory(Paths.get(this.path));

        if (!rootDirectory.isPresent()) {
            throw new TmcCoreException("Attempted to check code style for a non-project directory");
        }

        return taskExecutor.runCheckCodeStyle(rootDirectory.get());
    }
}
