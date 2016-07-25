package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;
import fi.helsinki.cs.tmc.core.holders.TmcLangsHolder;
import fi.helsinki.cs.tmc.core.holders.TmcSettingsHolder;
import fi.helsinki.cs.tmc.langs.abstraction.ValidationResult;
import fi.helsinki.cs.tmc.langs.domain.NoLanguagePluginFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/**
 * A {@link Command} for running code style validations on an exercise.
 */
public class RunCheckStyle extends Command<ValidationResult> {

    private static final Logger logger = LoggerFactory.getLogger(RunCheckStyle.class);

    private Exercise exercise;

    public RunCheckStyle(ProgressObserver observer, Exercise exercise) {
        super(observer);
        this.exercise = exercise;
    }

    @Override
    public ValidationResult call() throws TmcCoreException {
        logger.info("Running code style validation for exercise {}", exercise.getName());
        informObserver(0, "Running code style validation");

        Path path = exercise.getExerciseDirectory(TmcSettingsHolder.get().getTmcProjectDirectory());
        logger.debug("Determined exercise path: {}", path);

        try {
            logger.debug("Calling TMC langs");
            ValidationResult result = TmcLangsHolder.get()
                                            .runCheckCodeStyle(path, settings.getLocale());
            logger.debug("Received validation result");
            informObserver(1, "Finished running code style validation");
            return result;
        } catch (NoLanguagePluginFoundException ex) {
            informObserver(1, "Failed to run code style validation");
            logger.warn("Failed to run code style validations on target path", ex);
            throw new TmcCoreException("Unable to run code style validations on target path", ex);
        }
    }
}
