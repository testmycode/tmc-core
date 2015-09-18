package fi.helsinki.cs.tmc.core.communication;

import static com.google.common.base.Strings.isNullOrEmpty;

import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.util.Folders;
import fi.helsinki.cs.tmc.langs.util.TaskExecutor;
import fi.helsinki.cs.tmc.langs.util.TaskExecutorImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ExerciseDownloader {

    private static final Logger log = LoggerFactory.getLogger(ExerciseDownloader.class);

    private UrlCommunicator urlCommunicator;
    private TmcApi tmcApi;
    private TaskExecutor taskExecutor;
    private final ExecutorService downloadThreadPool;

    /**
     * Constructor for dependency injection.
     */
    public ExerciseDownloader(
            UrlCommunicator urlCommunicator, TmcApi tmcApi, TaskExecutor taskExecutor) {
        this.urlCommunicator = urlCommunicator;
        this.tmcApi = tmcApi;
        this.taskExecutor = taskExecutor;
        this.downloadThreadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    /**
     * Creates a new ExerciseDownloader instance.
     */
    public ExerciseDownloader(UrlCommunicator urlCommunicator, TmcApi tmcApi) {
        this(urlCommunicator, tmcApi, new TaskExecutorImpl());
    }

    /**
     * Method for downloading files if path where to download is defined. Also requires separate
     * folder name that will be created to defined path.
     *
     * @param exercises list of exercises which will be downloaded, list is parsed from json
     * @param path server path to exercises
     * @param folderName folder name of where exercises will be extracted (for example course name)
     */
    public void downloadExercises(
            List<Exercise> exercises, String path, String folderName, ExerciseObserver obs) {
        Map<Exercise, Future<Boolean>> futures = new HashMap<>();
        String coursePath = createCourseFolder(path, folderName);
        for (Exercise exercise : exercises) {
            Callable<Boolean> downloadHandler
                    = new SingleExerciseDownloadHandler(exercise, coursePath);
            futures.put(exercise, downloadThreadPool.submit(downloadHandler));
        }
        collectExerciseFutures(futures, obs);
    }

    private void collectExerciseFutures(
            Map<Exercise, Future<Boolean>> futures, ExerciseObserver obs) {
        for (Entry<Exercise, Future<Boolean>> future : futures.entrySet()) {
            try {
                obs.observe(future.getKey(), future.getValue().get());
            } catch (ExecutionException ex) {
                log.error("Failed to handle exercise: {}", ex);
            } catch (InterruptedException ex) {
                log.error("Handling of exercise was interrupted: {}", ex);
            }
        }
    }

    public String createCourseFolder(String path, String folderName) {
        path = formatPath(path);
        if (!isNullOrEmpty(folderName)) {
            path += folderName + File.separator;
        }
        File coursePath = new File(path);
        if (!coursePath.exists()) {
            coursePath.mkdirs();
        }
        return path;
    }

    /**
     * Handles downloading, unzipping & telling user information, for single exercise.
     *
     * @param exercise exercise which will be downloaded
     * @param path path where single exercise will be downloaded
     */
    public boolean handleSingleExercise(Exercise exercise, String path) {
        if (exercise.isLocked()) {
            return false;
        }
        Path filePath = Folders.tempFolder().resolve(exercise.getName() + ".zip");
        if(!downloadExerciseZip(exercise.getZipUrl(), filePath.toString())) {
            return false;
        }
        try {
            taskExecutor.extractProject(filePath, Paths.get(path));
        } catch (IOException e) {
            log.error("Could not extract archive: {}", path.toString());
            return false;
        } finally {
            deleteZip(filePath);
        }
        return true;
    }

    public boolean downloadModelSolution(Exercise exercise, Path targetPath) {
        Path zipPath = Folders.tempFolder().resolve(exercise.getName() + "-solution.zip");
        if(!downloadExerciseZip(exercise.getSolutionDownloadUrl(), zipPath.toString())) {
            return false;
        }
        try {
            taskExecutor.extractProject(zipPath, targetPath, true);
        } catch (IOException ex) {
            log.error("Could not download model solution: {}", ex);
            return false;
        } finally {
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Modify path to correct. Adds a trailing '/' if necessary.
     *
     * @param path the pathname to be corrected
     * @return corrected path
     */
    public String formatPath(String path) {
        if (path == null) {
            path = "";
        } else if (!path.isEmpty() && !path.endsWith(File.separator)) {
            path += File.separator;
        }
        return path;
    }

    /**
     * Downloads single .zip file by using URLCommunicator.
     *
     * @param zipUrl url which will be downloaded
     * @param path where to download
     */
    private boolean downloadExerciseZip(URI zipUrl, String path) {
        File file = new File(path);
        return urlCommunicator.downloadToFile(zipUrl, file);
    }

    private class SingleExerciseDownloadHandler implements Callable<Boolean> {

        private final Exercise exercise;
        private final String coursePath;

        public SingleExerciseDownloadHandler(Exercise exercise, String coursePath) {
            this.exercise = exercise;
            this.coursePath = coursePath;
        }

        @Override
        public Boolean call() {
            return handleSingleExercise(exercise, coursePath);
        }
    }
}
