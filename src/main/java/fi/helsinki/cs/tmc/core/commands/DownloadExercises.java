package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.ExerciseDownloader;
import fi.helsinki.cs.tmc.core.communication.TmcApi;
import fi.helsinki.cs.tmc.core.communication.UrlCommunicator;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;

import com.google.common.base.Optional;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

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
    private TmcApi tmcApi;
    private List<Exercise> exercisesToDownload;
    private ProgressObserver observer;
    private int courseId;
    private String path;

    public DownloadExercises(List<Exercise> exercisesToDownload, TmcSettings settings)
            throws TmcCoreException {
        super(settings);

        this.tmcApi = new TmcApi(settings);
        this.exerciseDownloader = new ExerciseDownloader(new UrlCommunicator(settings), tmcApi);
        this.exercisesToDownload = exercisesToDownload;
        this.path = settings.getTmcMainDirectory();

        Optional<Course> currentCourse = settings.getCurrentCourse();
        if (currentCourse.isPresent()) {
            this.courseId = currentCourse.get().getId();
        } else {
            throw new TmcCoreException("Unable to determine course, cannot download");
        }
    }

    public DownloadExercises(
            String path,
            int courseId,
            TmcSettings settings,
            ProgressObserver observer) {
        super(settings);

        this.path = path;
        this.courseId = courseId;
        this.tmcApi = new TmcApi(settings);
        this.exerciseDownloader = new ExerciseDownloader(new UrlCommunicator(settings), tmcApi);
        this.observer = observer;
    }

    public DownloadExercises(
            String path,
            int courseId,
            TmcSettings settings,
            File cacheFile,
            ProgressObserver observer) {
        this(path, courseId, settings, observer);

        this.cacheFile = cacheFile;
        this.tmcApi = new TmcApi(settings);
    }

    public DownloadExercises(
            ExerciseDownloader downloader,
            String path,
            int courseId,
            File cacheFile,
            TmcSettings settings,
            TmcApi tmcApi) {
        super(settings);

        this.exerciseDownloader = downloader;
        this.courseId = courseId;
        this.path = path;
        this.cacheFile = cacheFile;
        this.tmcApi = tmcApi;
    }

    public DownloadExercises(List<Exercise> exercises, TmcSettings settings, File updateCache)
            throws TmcCoreException {
        this(exercises, settings);
        this.cacheFile = updateCache;
    }

    public DownloadExercises(
            List<Exercise> exercises, TmcSettings settings, ProgressObserver observer)
            throws TmcCoreException {
        this(exercises, settings);
        this.observer = observer;
    }

    public DownloadExercises(
            List<Exercise> exercises,
            TmcSettings settings,
            File updateCache,
            ProgressObserver observer)
            throws TmcCoreException {
        this(exercises, settings, updateCache);
        this.observer = observer;
    }

    public boolean hasCacheFile() {
        return this.cacheFile != null;
    }

    /**
     * Parses the course JSON and executes downloading of the course exercises.
     */
    @Override
    public List<Exercise> call() throws TmcCoreException, IOException {
        if (!settings.userDataExists()) {
            throw new TmcCoreException("You need to login first.");
        }

        Optional<Course> courseResult = this.tmcApi.getCourse(this.courseId);

        if (!courseResult.isPresent()) {
            throw new TmcCoreException(
                    "Could not find the course. Please check your internet connection");
        }

        Course course = courseResult.get();
        return downloadExercisesFromList(getExercisesToDownload(course), course.getName());
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
     * @return a list of the exercises that were downloaded successfully
     */
    public List<Exercise> downloadExercisesFromList(List<Exercise> exercises, String courseName) {
        List<Exercise> downloadedExercises = new ArrayList<>();
        String courseFolderPath = exerciseDownloader.createCourseFolder(this.path, courseName);
        downloadExercises(exercises, courseName, downloadedExercises, courseFolderPath);
        cache(downloadedExercises);
        return downloadedExercises;
    }

    private void downloadExercises(
            List<Exercise> exercises,
            String courseName,
            List<Exercise> downloadedExercises,
            String path) {
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

    private void informProgressObserver(
            List<Exercise> exercises, int exCount, Exercise exercise, String status) {
        if (this.observer != null) {
            String message = "Downloading exercise " + exercise.getName() + " " + status;
            this.observer.progress(100.0 * exCount / exercises.size(), message);
        }
    }

    private void cache(List<Exercise> exercises) {
        if (this.cacheFile != null) {
            try {
                cacheExercises(exercises);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void cacheExercises(List<Exercise> exercises) throws IOException {
        Gson gson = new Gson();
        String json = FileUtils.readFileToString(cacheFile, Charset.forName("UTF-8"));
        Map<String, Map<String, String>> checksums = null;
        if (json != null && !json.isEmpty()) {
            Type typeOfHashMap = new TypeToken<Map<String, Map<String, String>>>() {}.getType();
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
            if (!checksums.containsKey(exercise.getCourseName())) {
                checksums.put(exercise.getCourseName(), new HashMap<String, String>());
            }
            checksums.get(exercise.getCourseName()).put(exercise.getName(), exercise.getChecksum());
        }
        try (FileWriter writer = new FileWriter(this.cacheFile)) {
            writer.write(gson.toJson(checksums, Map.class));
        }
    }
}
