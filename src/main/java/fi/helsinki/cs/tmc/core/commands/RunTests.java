package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.TmcJsonParser;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;
import fi.helsinki.cs.tmc.core.zipping.ProjectRootFinder;
import fi.helsinki.cs.tmc.langs.domain.NoLanguagePluginFoundException;
import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.util.TaskExecutor;
import fi.helsinki.cs.tmc.langs.util.TaskExecutorImpl;

import com.google.common.base.Optional;

import java.nio.file.Path;
import java.nio.file.Paths;

public class RunTests extends Command<RunResult> {

    private String path;

    public RunTests(String path, TmcSettings settings) {
        super(settings);
        this.path  = path;
    }

    @Override
    public RunResult call() throws TmcCoreException, NoLanguagePluginFoundException {
        TaskExecutor tmcLangs = new TaskExecutorImpl();
        TmcJsonParser parser = new TmcJsonParser(settings);
        ProjectRootFinder finder = new ProjectRootFinder(tmcLangs, parser);

        Optional<Path> exercise = finder.getRootDirectory(Paths.get(this.path));

        if (!exercise.isPresent()) {
            throw new TmcCoreException("Target path for runExercise is not an exercise: " + path);
        }

        return tmcLangs.runTests(exercise.get());
    }
}
