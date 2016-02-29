package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.TmcServerCommunicationTaskFactory;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.exceptions.TmcInterruptionException;
import fi.helsinki.cs.tmc.core.holders.TmcLangsHolder;
import fi.helsinki.cs.tmc.core.holders.TmcSettingsHolder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * A {@link Command} for downloading exercises.
 */
public class DownloadOrUpdateExercises extends Command<List<Exercise>> {

    private static final Logger logger
            = LoggerFactory.getLogger(DownloadOrUpdateExercises.class);

    private List<Exercise> exercises;

    public DownloadOrUpdateExercises(ProgressObserver observer, List<Exercise> exercises) {
        super(observer);
        this.exercises = exercises;
    }

    @Override
    public List<Exercise> call() throws TmcInterruptionException {
        List<Exercise> successfullyDownloaded = new ArrayList<>();

        for (Exercise exercise : exercises) {

            //TODO: Multi-thread?

            checkInterrupt();


            byte[] zip;
            try {
                zip = new TmcServerCommunicationTaskFactory()
                        .getDownloadingExerciseZipTask(exercise)
                        .call();
            } catch (Exception ex) {
                logger.warn("Failed to download project from TMC-server", ex);
                continue;
            }

            checkInterrupt();

            Path exerciseZipTemporaryPath;
            try {
                exerciseZipTemporaryPath = writeToTemp(zip);
            } catch (IOException ex) {
                logger.warn("Failed to write downloaded zip to disk", ex);
                continue;
            }

            checkInterrupt();

            Path tmcRoot = TmcSettingsHolder.get().getTmcProjectDirectory();
            Path target = exercise.getExtractionTarget(tmcRoot);
            try {
                TmcLangsHolder.get().extractProject(exerciseZipTemporaryPath, target);
            } catch (Exception ex) {
                logger.warn("Failed to extract project from " + exerciseZipTemporaryPath
                        + " to " + target, ex);
                continue;
            } finally {
                cleanUp(exerciseZipTemporaryPath);
            }

            successfullyDownloaded.add(exercise);
            //TODO: Update PluginState

            //TODO: Make into future / callable / something?
        }
        return successfullyDownloaded;
    }

    private void cleanUp(Path zip) {
        try {
            Files.deleteIfExists(zip);
        } catch (IOException ex) {
            logger.warn("Failed to delete temporary exercise zip from " + zip, ex);
        }
    }

    private Path writeToTemp(byte[] zip) throws IOException {
        Path target = Files.createTempFile("tmc-exercise-", ".zip");
        Files.write(target, zip);
        return target;
    }
}
