package fi.helsinki.cs.tmc.core.communication;

import static com.google.common.base.Strings.isNullOrEmpty;

import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.util.Folders;
import fi.helsinki.cs.tmc.langs.util.TaskExecutor;
import fi.helsinki.cs.tmc.langs.util.TaskExecutorImpl;

import com.google.common.base.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ExerciseDownloader {

    private static final Logger log = LoggerFactory.getLogger(ExerciseDownloader.class);

    private UrlCommunicator urlCommunicator;
    private TmcApi tmcApi;
    private TaskExecutor taskExecutor;

    /**
     * Constructor for dependency injection.
     */
    public ExerciseDownloader(
            UrlCommunicator urlCommunicator, TmcApi tmcApi, TaskExecutor taskExecutor) {
        this.urlCommunicator = urlCommunicator;
        this.tmcApi = tmcApi;
        this.taskExecutor = taskExecutor;
    }

    /**
     * Creates a new ExerciseDownloader instance.
     */
    public ExerciseDownloader(UrlCommunicator urlCommunicator, TmcApi tmcApi) {
        this(urlCommunicator, tmcApi, new TaskExecutorImpl());
    }

    /**
     * Download exercises by course url.
     *
     * @param courseUrl course url
     * @return info about downloading.
     */
    public Optional<List<Exercise>> downloadExercises(URI courseUrl) throws IOException {
        List<Exercise> exercises = tmcApi.getExercises(courseUrl);
        if (exercises.isEmpty()) {
            return Optional.absent();
        }
        return downloadFiles(exercises);
    }

    /**
     * Method for downloading files if path is not defined.
     *
     * @param exercises list of exercises which will be downloaded, list is parsed from json.
     * @return info about downloading.
     */
    public Optional<List<Exercise>> downloadFiles(List<Exercise> exercises) throws IOException {
        return downloadFiles(exercises, Paths.get(""));
    }

    /**
     * Method for downloading files if path where to download is defined.
     *
     * @return info about downloading.
     */
    public Optional<List<Exercise>> downloadFiles(List<Exercise> exercises, Path path) throws IOException {
        return downloadFiles(exercises, path, null);
    }

    /**
     * Method for downloading files if path where to download is defined. Also requires separate
     * folder name that will be created to defined path.
     *
     * @param exercises list of exercises which will be downloaded, list is parsed from json.
     * @param path server path to exercises.
     * @param folderName folder name of where exercises will be extracted (for example course name)
     */
    public Optional<List<Exercise>> downloadFiles(
            List<Exercise> exercises, Path path, String folderName) throws IOException {
        List<Exercise> downloadedExercises = new ArrayList<>();
        path = createCourseFolder(path, folderName);
        for (Exercise exercise : exercises) {
            boolean success = handleSingleExercise(exercise, path);
            if (success) {
                downloadedExercises.add(exercise);
            }
        }

        return Optional.of(downloadedExercises);
    }

    public Path createCourseFolder(Path path, String folderName) throws IOException {
        if (!isNullOrEmpty(folderName)) {
            path = path.resolve(folderName);
        }
        if (Files.notExists(path)) {
            Files.createDirectories(path);
        }
        return path;
    }

    /**
     * Handles downloading, unzipping & telling user information, for single exercise.
     *
     * @param exercise Exercise which will be downloaded
     * @param path path where single exercise will be downloaded
     */
    public boolean handleSingleExercise(Exercise exercise, Path path) {
        if (exercise.isLocked()) {
            return false;
        }
        Path filePath = Folders.tempFolder().resolve(exercise.getName() + ".zip");
        downloadExerciseZip(exercise.getZipUrl(), filePath);
        try {
            taskExecutor.extractProject(filePath, path);
        }
        catch (IOException e) {
            log.error("Could not extract archive: {}", path.toString());
            return false;
        }
        finally {
            deleteZip(filePath);
        }
        return true;
    }

    public boolean downloadModelSolution(Exercise exercise, Path targetPath) {
        Path zipPath = Folders.tempFolder().resolve(exercise.getName() + "-solution.zip");
        downloadExerciseZip(exercise.getSolutionDownloadUrl(), zipPath);
        try {
            taskExecutor.extractProject(zipPath, targetPath, true);
        }
        catch (IOException ex) {
            log.error("Could not download model solution: {}", ex);
            return false;
        }
        finally {
            deleteZip(zipPath);
        }
        return true;
    }

    /**
     * Delete .zip -file after unzipping.
     *
     * @param filePath path to delete
     */
    private void deleteZip(Path filePath) {
        try {
            Files.delete(filePath);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Get advantage percent in downloading single exercise.
     *
     * @param exCount order number of exercise in downloading
     * @param exercisesSize total amount of exercises that will be downloaded
     * @return percents
     */
    public double getPercents(int exCount, int exercisesSize) {
        return Math.round(1.0 * exCount / exercisesSize * 100);
    }

    /**
     * Downloads single .zip file by using URLCommunicator.
     *
     * @param zipUrl url which will be downloaded
     * @param path where to download
     */
    private void downloadExerciseZip(URI zipUrl, Path path) {
        urlCommunicator.downloadToFile(zipUrl, path);
    }
}
