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

    private TaskExecutorImpl taskExecutor;
    private ProjectRootFinder rootfinder;

    public RunCheckStyle(String path, TmcSettings settings) {
        this(settings);
        this.setParameter("path", path);
    }

    public RunCheckStyle(TmcSettings settings) {
        this(new TaskExecutorImpl(), new ProjectRootFinder(new TmcJsonParser(settings)), settings);
    }

    public RunCheckStyle(TaskExecutorImpl executor, ProjectRootFinder finder, TmcSettings settings) {
        super(settings);
        rootfinder = finder;
        taskExecutor = executor;
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
    }

    @Override
    public ValidationResult call() throws TmcCoreException, NoLanguagePluginFoundException {
        String path = this.data.get("path");
        Optional<Path> exercise = rootfinder.getRootDirectory(Paths.get(path));
        if (!exercise.isPresent()) {
            throw new TmcCoreException("Not an exercise. (null)");
        }
        return runCheckStyle(exercise.get());
    }
}

