package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;
import fi.helsinki.cs.tmc.core.holders.TmcLangsHolder;
import fi.helsinki.cs.tmc.core.holders.TmcSettingsHolder;
import fi.helsinki.cs.tmc.langs.domain.NoLanguagePluginFoundException;
import fi.helsinki.cs.tmc.langs.domain.RunResult;

import com.google.common.annotations.VisibleForTesting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/**
 * A {@link Command} for running test for an exercise.
 */
public class RunTests extends Command<RunResult> {

    private static final Logger logger = LoggerFactory.getLogger(RunTests.class);

    private Exercise exercise;

    public RunTests(ProgressObserver observer, Exercise exercise) {
        super(observer);
        this.exercise = exercise;
    }

    @Override
    public RunResult call() throws TmcCoreException {
        Path path = exercise.getExtractionTarget(TmcSettingsHolder.get().getTmcProjectDirectory());
        try {
            return TmcLangsHolder.get().runTests(path);
        } catch (NoLanguagePluginFoundException ex) {
            logger.warn("Failed to run tests for project", ex);
            throw new TmcCoreException("Failed to run tests for project", ex);
        }
    }
}
