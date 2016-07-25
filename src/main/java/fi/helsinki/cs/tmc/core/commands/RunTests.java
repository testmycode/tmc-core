package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;
import fi.helsinki.cs.tmc.core.holders.TmcLangsHolder;
import fi.helsinki.cs.tmc.core.holders.TmcSettingsHolder;
import fi.helsinki.cs.tmc.langs.domain.NoLanguagePluginFoundException;
import fi.helsinki.cs.tmc.langs.domain.RunResult;

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
        logger.info("Running tests for exercise {}", exercise.getName());
        informObserver(0, "Running tests");

        Path path = exercise.getExerciseDirectory(TmcSettingsHolder.get().getTmcProjectDirectory());
        logger.debug("Determined project path as {}", path);

        try {
            logger.debug("Calling TMC Langs");
            RunResult result = TmcLangsHolder.get().runTests(path);
            logger.debug("Successfully ran tests");
            informObserver(1, "Finished running tests");
            return result;
        } catch (NoLanguagePluginFoundException ex) {
            informObserver(1, "Failed to run tests");
            logger.warn("Failed to run tests for project", ex);
            throw new TmcCoreException("Failed to run tests for project", ex);
        }
    }
}
