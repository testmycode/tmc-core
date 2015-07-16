package hy.tmc.core.commands;

import com.google.common.base.Optional;
import fi.helsinki.cs.tmc.langs.domain.NoLanguagePluginFoundException;
import fi.helsinki.cs.tmc.langs.util.TaskExecutorImpl;
import fi.helsinki.cs.tmc.stylerunner.validation.ValidationResult;
import hy.tmc.core.communication.TmcJsonParser;
import hy.tmc.core.configuration.TmcSettings;
import hy.tmc.core.exceptions.TmcCoreException;
import hy.tmc.core.zipping.DefaultRootDetector;
import hy.tmc.core.zipping.ProjectRootFinder;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RunCheckStyle extends Command<ValidationResult> {
    
    private ProjectRootFinder finder;
    private TaskExecutorImpl taskExecutor;
    private ProjectRootFinder rootfinder;

    /**
     * Default constructor.
     * @param path to exercise, that should be passed to TmcLangs checkstyle-runner.
     * @param settings containing at least credentials and serverAddress to
     * enable communication with server.
     */
    public RunCheckStyle(String path, TmcSettings settings) {
        this(path, settings, new ProjectRootFinder(
                new DefaultRootDetector(), new TmcJsonParser(settings)),
                new TaskExecutorImpl()
        );
    }

    /**
     * Constructor for dependency injection in tests.
     */
    public RunCheckStyle(String path,
                         TmcSettings settings,
                         ProjectRootFinder finder,
                         TaskExecutorImpl executor) {
        super(settings);
        this.setParameter("path", path);
        this.finder= finder;
        this.taskExecutor = executor;
    }
    
    /**
     * Runs checkstyle for exercise.
     *
     * @param exercise Path object
     * @return String contaning results
     * @throws NoLanguagePluginFoundException if path doesn't contain exercise
     */
    public ValidationResult runCheckStyle(Path exercise) throws NoLanguagePluginFoundException {
        return taskExecutor.runCheckCodeStyle(exercise);
    }

    @Override
    public void checkData() throws TmcCoreException {
        if (!this.data.containsKey("path") || this.data.get("path").isEmpty()) {
            throw new TmcCoreException("File path to exercise required.");
        }
        if (this.settingsNotPresent()) {
            throw new TmcCoreException("Credentials and serverAddress are required "
                    + "for server communication.");
        }
    }

    @Override
    public ValidationResult call() throws TmcCoreException, NoLanguagePluginFoundException {
        String path = this.data.get("path");
        Optional<Path> rootDirectory = finder.getRootDirectory(Paths.get(path));
        if (!rootDirectory.isPresent()) {
            throw new TmcCoreException("Not an exercise. (null)");
        }
        return runCheckStyle(rootDirectory.get());
    }
}

