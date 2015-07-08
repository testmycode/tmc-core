package hy.tmc.core.communication;

import com.google.common.base.Optional;
import static com.google.common.base.Strings.isNullOrEmpty;

import hy.tmc.core.domain.Exercise;

import hy.tmc.core.zipping.UnzipDecider;
import hy.tmc.core.zipping.Unzipper;

import net.lingala.zip4j.exception.ZipException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ExerciseDownloader {
    
    private UnzipDecider decider;
    private File cacheFile;
    private UrlCommunicator urlCommunicator;
    private TmcJsonParser tmcJsonParser;
    private Unzipper unzipper;

    /**
     * Constructor for dependency injection.
     *
     * @param decider UnzipDecider which decides which files to unzip
     */
    public ExerciseDownloader(UnzipDecider decider,
            UrlCommunicator urlCommunicator, TmcJsonParser tmcJsonParser) {
        this.decider = decider;
        this.urlCommunicator = urlCommunicator;
        this.tmcJsonParser = tmcJsonParser;
    }

    /**
     * Constructor for tests
     *
     * @param decider UnzipDecider which decides which files to unzip
     */
    public ExerciseDownloader(UnzipDecider decider,
            UrlCommunicator urlCommunicator, TmcJsonParser tmcJsonParser, Unzipper zipHandler) {
        this(decider, urlCommunicator, tmcJsonParser);
        this.unzipper = zipHandler;
    }

    /**
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
        int exCount = 0;
        for (Exercise exercise : exercises) {
            boolean success = handleSingleExercise(exercise, exCount, exercises.size(), path);
            if (success) {
                downloadedExercises.add(exercise);
            }
            exCount++;
        }
        
        return Optional.of(downloadedExercises);
    }

    /**
     * Handles downloading, unzipping & telling user information, for single exercise.
     *
     * @param exercise Exercise which will be downloaded
     * @param exCount order number of exercise in downloading
     * @param totalCount amount of all exercises
     * @param path path where single exercise will be downloaded
     */
    public boolean handleSingleExercise(Exercise exercise, int exCount, int totalCount, String path) {
        if (exercise.isLocked()) {
            return false;
        }
        String filePath = path + exercise.getName() + ".zip";
        downloadExerciseZip(exercise.getZipUrl(), filePath);
        try {
            unzipFile(filePath, path);
        } catch (IOException | ZipException ex) {
            System.err.println(ex.getMessage());
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
        File file = new File(filePath);
        file.delete();
    }

    /**
     * Unzips single file after downloading.
     *
     * @param unzipPath path of file which will be unzipped
     * @param destinationPath destination path
     */
    public void unzipFile(String unzipPath, String destinationPath) throws IOException, ZipException {
        if (unzipper == null) {
            unzipper = new Unzipper(unzipPath, destinationPath, decider);
        } else {
            unzipper.setZipPath(unzipPath);
            unzipper.setUnzipLocation(destinationPath);
        }
        unzipper.unzip();
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
