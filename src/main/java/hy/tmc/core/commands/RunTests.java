package hy.tmc.core.commands;

import com.google.common.base.Optional;

import fi.helsinki.cs.tmc.langs.domain.NoLanguagePluginFoundException;
import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.util.TaskExecutorImpl;

import hy.tmc.core.communication.TmcJsonParser;
import hy.tmc.core.configuration.TmcSettings;
import hy.tmc.core.exceptions.TmcCoreException;
import hy.tmc.core.zipping.ProjectRootFinder;

import java.nio.file.Path;
import java.nio.file.Paths;

public class RunTests extends Command<RunResult> {

    public RunTests(String path, TmcSettings settings) {
        super(settings);
        this.setParameter("path", path);
    }

    public RunTests(TmcSettings settings) {
        super(settings);
    }

    /**
     * Runs tests for exercise.
     *
     * @param exercise Path object
     * @return String contaning results
     * @throws NoLanguagePluginFoundException if path doesn't contain exercise
     */
    public RunResult runTests(Path exercise) throws NoLanguagePluginFoundException {
        System.err.println("Path: " + exercise.toString());
        TaskExecutorImpl taskExecutor = new TaskExecutorImpl();
        return taskExecutor.runTests(exercise);
    }

    @Override
    public void checkData() throws TmcCoreException {
        if (!this.data.containsKey("path") || this.data.get("path").isEmpty()) {
            throw new TmcCoreException("File path to exercise required.");
        }
    }

    @Override
    public RunResult call() throws TmcCoreException, NoLanguagePluginFoundException {
        String path = (String) this.data.get("path");
        ProjectRootFinder finder =
                new ProjectRootFinder(new TaskExecutorImpl(), new TmcJsonParser(settings));
        Optional<Path> exercise = finder.getRootDirectory(Paths.get(path));
        if (!exercise.isPresent()) {
            throw new TmcCoreException("Not an exercise. (null)");
        }
        return runTests(exercise.get());
    }
}
