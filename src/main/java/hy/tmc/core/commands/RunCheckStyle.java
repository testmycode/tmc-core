package hy.tmc.core.commands;

import com.google.common.base.Optional;
import fi.helsinki.cs.tmc.langs.NoLanguagePluginFoundException;
import fi.helsinki.cs.tmc.langs.RunResult;
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

    public RunCheckStyle(String path, TmcSettings settings) {
        super(settings);
        this.setParameter("path", path);
    }

    public RunCheckStyle(TmcSettings settings) {
        super(settings);
    }
    
    /**
     * Runs checkstyle for exercise.
     *
     * @param exercise Path object
     * @return String contaning results
     * @throws NoLanguagePluginFoundException if path doesn't contain exercise
     */
    public ValidationResult runCheckStyle(Path exercise) throws NoLanguagePluginFoundException {
        TaskExecutorImpl taskExecutor = new TaskExecutorImpl();
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
        String path = (String) this.data.get("path");
        ProjectRootFinder finder = new ProjectRootFinder(new DefaultRootDetector(), new TmcJsonParser(settings));
        Optional<Path> exercise = finder.getRootDirectory(Paths.get(path));
        if (!exercise.isPresent()) {
            throw new TmcCoreException("Not an exercise. (null)");
        }
        return runCheckStyle(exercise.get());
    }
}

