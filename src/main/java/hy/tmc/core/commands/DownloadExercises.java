package hy.tmc.core.commands;

import com.google.common.base.Optional;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import hy.tmc.core.communication.ExerciseDownloader;
import hy.tmc.core.communication.TmcJsonParser;
import hy.tmc.core.communication.UrlCommunicator;
import hy.tmc.core.configuration.TmcSettings;
import hy.tmc.core.domain.Course;
import hy.tmc.core.domain.Exercise;
import hy.tmc.core.domain.ProgressObserver;
import hy.tmc.core.exceptions.TmcCoreException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DownloadExercises extends Command<List<Exercise>> {

    /**
     * ExerciseDownloader that is used for downloading.
     */
    private ExerciseDownloader exerciseDownloader;
    private File cacheFile;
    private TmcJsonParser parser;
    private List<Exercise> exercisesToDownload;
    private ProgressObserver observer;

    public DownloadExercises(List<Exercise> exercisesToDownload, TmcSettings settings) throws TmcCoreException {
        super(settings);
        this.parser = new TmcJsonParser(settings);
        this.exerciseDownloader = new ExerciseDownloader(
                new UrlCommunicator(settings), new TmcJsonParser(settings));
        this.exercisesToDownload = exercisesToDownload;
        Optional<Course> currentCourse = settings.getCurrentCourse();
        String mainDirectory = settings.getTmcMainDirectory();
        if (currentCourse.isPresent()) {
            Course course = currentCourse.get();
            this.setParameter("courseID", "" + course.getId());
            this.setParameter("path", mainDirectory);
        } else {
            throw new TmcCoreException("Unable to determine course, cannot download");
        }
    }

    public DownloadExercises(String path, String courseId,
            TmcSettings settings, ProgressObserver observer) {
        super(settings);
        this.setParameter("path", path);
        this.setParameter("courseID", courseId);
        this.parser = new TmcJsonParser(settings);
        this.exerciseDownloader = new ExerciseDownloader(
                new UrlCommunicator(settings), new TmcJsonParser(settings));
        this.observer = observer;
    }

    public DownloadExercises(String path, String courseId, TmcSettings settings,
            File cacheFile, ProgressObserver observer) {
        this(path, courseId, settings, observer);
        this.cacheFile = cacheFile;
        this.parser = new TmcJsonParser(settings);
    }

    public DownloadExercises(ExerciseDownloader downloader, String path, String courseId,
            File cacheFile, TmcSettings settings, TmcJsonParser parser) {
        super(settings);
        this.exerciseDownloader = downloader;
        this.setParameter("path", path);
        this.setParameter("courseID", courseId);
        this.cacheFile = cacheFile;
        this.parser = parser;
    }

    public DownloadExercises(List<Exercise> exercises, TmcSettings settings, File updateCache) throws TmcCoreException {
        this(exercises, settings);
        this.cacheFile = updateCache;
    }

    public DownloadExercises(List<Exercise> exercises, TmcSettings settings, ProgressObserver observer) throws TmcCoreException {
        this(exercises, settings);
        this.observer = observer;
    }

    public DownloadExercises(List<Exercise> exercises, TmcSettings settings, File updateCache, ProgressObserver observer) throws TmcCoreException {
        this(exercises, settings, updateCache);
        this.observer = observer;
    }

    /**
     * Checks that command has required parameters courseID is the id of the course and path is the
     * path of where files are downloaded and extracted.
     *
     * @throws TmcCoreException if path isn't supplied
     */
    @Override
    public void checkData() throws TmcCoreException {
        checkCourseId();
        if (!this.data.containsKey("path")) {
            throw new TmcCoreException("Path required");
        }
        if (!settings.userDataExists()) {
            throw new TmcCoreException("You need to login first.");
        }
    }

    /**
     * Check that user has given also course id.
     *
     * @throws TmcCoreException if course id is not a number
     */
    private void checkCourseId() throws TmcCoreException {
        if (!this.data.containsKey("courseID")) {
            throw new TmcCoreException("Course ID required");
        }
        try {
            int courseId = Integer.parseInt(this.data.get("courseID"));
        }
        catch (NumberFormatException e) {
            throw new TmcCoreException("Given course id is not a number");
        }
    }

    public boolean cacheFileSet() {
        return this.cacheFile != null;
    }

    /**
     * Parses the course JSON and executes downloading of the course exercises.
     */
    @Override
    public List<Exercise> call() throws TmcCoreException, IOException {
        checkData();
        Optional<Course> courseResult = this.parser.getCourse(Integer.parseInt(this.data.get("courseID")));
        if (courseResult.isPresent()) {
            Course course = courseResult.get();
            List<Exercise> downloadResult = downloadExercisesFromList(getExercisesToDownload(course), course.getName());
            return downloadResult;
        }

        throw new TmcCoreException("Could not find the course. Please check your internet connection");
    }

    private List<Exercise> getExercisesToDownload(Course course) {
        if (this.exercisesToDownload == null) {
            return course.getExercises();
        }
        return this.exercisesToDownload;
    }

    /**
     * Download exercises to under the directory specified by the path in the data map.
     *
     * @param exercises
     * @param courseName
     * @return a list of the exercises that were downloaded successfully
     */
    public List<Exercise> downloadExercisesFromList(List<Exercise> exercises, String courseName) {
        List<Exercise> downloadedExercises = new ArrayList<>();
        String courseFolderPath = exerciseDownloader.createCourseFolder(data.get("path"), courseName);
        downloadExercises(exercises, courseName, downloadedExercises, courseFolderPath);
        cache(downloadedExercises);
        return downloadedExercises;
    }

    private void downloadExercises(List<Exercise> exercises, String courseName, List<Exercise> downloadedExercises, String path) {
        int exCount = 0;
        for (Exercise exercise : exercises) {
            exercise.setCourseName(courseName);
            boolean downloadSuccessful = exerciseDownloader.handleSingleExercise(exercise, path);
            exCount++;
            String status = "failed";
            if (downloadSuccessful) {
                downloadedExercises.add(exercise);
                status = "was succesful";
            }
            informProgressObserver(exercises, exCount, exercise, status);
        }
    }

    private void informProgressObserver(List<Exercise> exercises, int exCount, Exercise exercise, String status) {
        if (this.observer != null) {
            String message = "Downloading exercise " + exercise.getName() + " " + status;
            this.observer.progress(100.0 * exCount / exercises.size(), message);
        }
    }

    private void cache(List<Exercise> exercises) {
        if (this.cacheFile != null) {
            try {
                cacheExercises(exercises);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void cacheExercises(List<Exercise> exercises) throws IOException {
        Gson gson = new Gson();
        String json = FileUtils.readFileToString(cacheFile, Charset.forName("UTF-8"));
        Map<Integer, String> checksums = null;
        if (json != null && !json.isEmpty()) {
            Type typeOfHashMap = new TypeToken<Map<Integer, String>>() {
            }.getType();
            try {
                checksums = gson.fromJson(json, typeOfHashMap);
            } catch (JsonSyntaxException ex) {
                System.err.println("WARNING: corrupt cachefile, ignoring and overwriting");
                checksums = new HashMap<>();
            }
        }
        if (checksums == null) {
            checksums = new HashMap<>();
        }

        for (Exercise exercise : exercises) {
            checksums.put(exercise.getId(), exercise.getChecksum());
        }
        try (FileWriter writer = new FileWriter(this.cacheFile)) {
            writer.write(gson.toJson(checksums, Map.class));
        }
    }
}
