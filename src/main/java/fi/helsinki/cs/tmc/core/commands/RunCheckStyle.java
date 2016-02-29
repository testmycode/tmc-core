package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;
import fi.helsinki.cs.tmc.core.holders.TmcLangsHolder;
import fi.helsinki.cs.tmc.core.holders.TmcSettingsHolder;
import fi.helsinki.cs.tmc.langs.abstraction.ValidationResult;
import fi.helsinki.cs.tmc.langs.domain.NoLanguagePluginFoundException;

import java.nio.file.Path;

/**
 * A {@link Command} for running code style validations on an exercise.
 */
public class RunCheckStyle extends Command<ValidationResult> {

    private Exercise exercise;

    public RunCheckStyle(ProgressObserver observer, Exercise exercise) {
        super(observer);
        this.exercise = exercise;
    }

    @Override
    public ValidationResult call() throws TmcCoreException {
        Path path = exercise.getExtractionTarget(TmcSettingsHolder.get().getTmcProjectDirectory());
        try {
            return TmcLangsHolder.get().runCheckCodeStyle(path, settings.getLocale());
        } catch (NoLanguagePluginFoundException ex) {
            throw new TmcCoreException(
                    "Unable to run code style validations on target path",
                    ex);
        }
    }
}
