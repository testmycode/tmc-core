package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.TmcServerCommunicationTaskFactory;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;
import fi.helsinki.cs.tmc.core.holders.TmcLangsHolder;
import fi.helsinki.cs.tmc.core.holders.TmcSettingsHolder;
import fi.helsinki.cs.tmc.langs.domain.NoLanguagePluginFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractSubmissionCommand<T> extends Command<T> {

    private static final Logger logger
            = LoggerFactory.getLogger(AbstractSubmissionCommand.class);

    public AbstractSubmissionCommand(ProgressObserver observer) {
        super(observer);
    }

    public TmcServerCommunicationTaskFactory.SubmissionResponse submitToServer(
            Exercise exercise,
            Map<String, String> extraParams)
            throws TmcCoreException {

        byte[] zippedProject;

        Path tmcRoot = TmcSettingsHolder.get().getTmcProjectDirectory();
        Path projectPath = exercise.getExtractionTarget(tmcRoot);
        try {
            zippedProject = TmcLangsHolder.get().compressProject(projectPath);
        } catch (IOException | NoLanguagePluginFoundException ex) {
            logger.warn("Failed to compress project", ex);
            throw new TmcCoreException("Failed to compress project", ex);
        }

        extraParams.put("error_msg_locale", TmcSettingsHolder.get().getLocale().toString());
        try {
            return new TmcServerCommunicationTaskFactory().getSubmittingExerciseTask(
                    exercise,
                    zippedProject,
                    new HashMap<String, String>())
                    .call();
        } catch (Exception ex) {
            logger.warn("Failed to submit exercise", ex);
            throw new TmcCoreException("Failed to submit exercise", ex);
        }
    }
}
