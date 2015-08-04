package hy.tmc.core.communication;

import com.google.common.base.Optional;
import fi.helsinki.cs.tmc.langs.util.TaskExecutor;
import fi.helsinki.cs.tmc.langs.util.TaskExecutorImpl;
import hy.tmc.core.domain.Exercise;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;

public class ExerciseDownloader {
    
    private File cacheFile;
    private UrlCommunicator urlCommunicator;
    private TmcJsonParser tmcJsonParser;
    private TaskExecutor taskExecutor;

    /**
     * Constructor for dependency injection.
     */
    public ExerciseDownloader(UrlCommunicator urlCommunicator, TmcJsonParser tmcJsonParser,
                              TaskExecutor taskExecutor) {
        this.urlCommunicator = urlCommunicator;
        this.tmcJsonParser = tmcJsonParser;
        this.taskExecutor = taskExecutor;
    }

    /**
     * Creates a new ExerciseDownloader instance
     */
    public ExerciseDownloader(UrlCommunicator urlCommunicator, TmcJsonParser tmcJsonParser) {
        this(urlCommunicator, tmcJsonParser, new TaskExecutorImpl());
    }


    /**s
     * Download exercises by course url.
     *
     * @param courseUrl course url
     * @return info about downloading.
     */
    public Optional<List<Exercise>> downloadExercises(String courseUrl) throws IOException {
        List<Exercise> exercises = tmcJsonParser.getExercises(courseUrl);
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
    public Optional<List<Exercise>> downloadFiles(List<Exercise> exercises) {
        return downloadFiles(exercises, "");
    }

    /**
     * Method for downloading files if path where to download is defined.
     *
     * @return info about downloading.
     */
    public Optional<List<Exercise>> downloadFiles(List<Exercise> exercises, String path) {
        return downloadFiles(exercises, path, null);
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
     * Method for downloading files if path where to download is defined. Also requires separate
     * folder name that will be created to defined path.
     *
     * @param exercises list of exercises which will be downloaded, list is parsed from json.
     * @param path server path to exercises.
     * @param folderName folder name of where exercises will be extracted (for example course name)
     * @return
     */
    public Optional<List<Exercise>> downloadFiles(List<Exercise> exercises, String path, String folderName) {
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

    /**
     * Handles downloading, unzipping & telling user information, for single exercise.
     *
     * @param exercise Exercise which will be downloaded
     * @param path path where single exercise will be downloaded
     */
    public boolean handleSingleExercise(Exercise exercise, String path) {
        if (exercise.isLocked()) {
            return false;
        }
        String filePath = path + exercise.getName() + ".zip";
        downloadExerciseZip(exercise.getZipUrl(), filePath);
        try {
            taskExecutor.extractProject(Paths.get(filePath), Paths.get(path));
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return false;
        } finally {
            deleteZip(filePath);
        }
        return true;
    }

    /**
     * Delete .zip -file after unzipping.
     *
     * @param filePath path to delete
     */
    private void deleteZip(String filePath) {
        try {
            Files.delete(Paths.get(filePath));
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
    private void downloadExerciseZip(String zipUrl, String path) {
        File file = new File(path);
        urlCommunicator.downloadToFile(zipUrl, file);
    }
}
