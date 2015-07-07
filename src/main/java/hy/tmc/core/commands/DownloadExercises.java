package hy.tmc.core.commands;

import com.google.common.base.Optional;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import hy.tmc.core.communication.ExerciseDownloader;
import hy.tmc.core.communication.TmcJsonParser;
import hy.tmc.core.communication.UrlCommunicator;
import hy.tmc.core.configuration.TmcSettings;
import hy.tmc.core.domain.Course;
import hy.tmc.core.domain.Exercise;
import hy.tmc.core.exceptions.TmcCoreException;
import hy.tmc.core.zipping.DefaultUnzipDecider;
import hy.tmc.core.zipping.UnzipDecider;
import java.io.File;
import java.io.FileWriter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import java.util.List;

public class DownloadExercises extends Command<String> {

    /**
     * ExerciseDownloader that is used for downloading.
     */
    private ExerciseDownloader exerciseDownloader;
    private File cacheFile;
    private TmcJsonParser parser;
    private List<Exercise> exercisesToDownload;

    public DownloadExercises(TmcSettings settings) {
        super(settings);
        this.exerciseDownloader = new ExerciseDownloader(new DefaultUnzipDecider(),
                new UrlCommunicator(settings), new TmcJsonParser(settings));
        this.parser = new TmcJsonParser(settings);
    }

    public DownloadExercises(TmcSettings settings, TmcJsonParser parser) {
        super(settings);
        this.parser = parser;
    }

    public DownloadExercises(List<Exercise> exercisesToDownload, TmcSettings settings) throws TmcCoreException {
        super(settings);
        this.exerciseDownloader = new ExerciseDownloader(new DefaultUnzipDecider(),
                new UrlCommunicator(settings), new TmcJsonParser(settings));
        this.exercisesToDownload = exercisesToDownload;
        Optional<Course> course = settings.getCurrentCourse();
        if (course.isPresent()) {
            this.setParameter("courseID", "" + course.get().getId());
        } else {
            throw new TmcCoreException("Unable to determine course, cannot download");
        }

    }

    public DownloadExercises(String path, String courseId, TmcSettings settings) {
        this(settings);
        this.setParameter("path", path);
        this.setParameter("courseID", courseId);
        this.parser = new TmcJsonParser(settings);
    }

    public DownloadExercises(String path, String courseId, TmcSettings settings, File cacheFile) throws IOException {
        this(path, courseId, settings);
        this.cacheFile = cacheFile;
        this.parser = new TmcJsonParser(settings);
    }

    public DownloadExercises(ExerciseDownloader downloader, String path, String courseId, File cacheFile, TmcSettings settings) {
        this.settings = settings;
        this.exerciseDownloader = downloader;
        this.setParameter("path", path);
        this.setParameter("courseID", courseId);
        this.cacheFile = cacheFile;
        this.parser = new TmcJsonParser(settings);
    }

    public DownloadExercises(ExerciseDownloader downloader, String path, String courseId, File cacheFile, TmcSettings settings, TmcJsonParser parser) {
        this.settings = settings;
        this.exerciseDownloader = downloader;
        this.setParameter("path", path);
        this.setParameter("courseID", courseId);
        this.cacheFile = cacheFile;
        this.parser = parser;
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
     *
     * @return
     */
    @Override
    public String call() throws TmcCoreException, IOException {
        checkData();

        Optional<Course> courseResult = this.parser.getCourse(Integer.parseInt(this.data.get("courseID")));
        
        if (courseResult.isPresent()) {
            Course course = courseResult.get();
            Optional<String> downloadResult = downloadExercisesFromList(getExercisesToDownload(course), course.getName());
            if (downloadResult.isPresent()) {
                return downloadResult.get();
            }
        }

        throw new TmcCoreException("Failed to fetch exercises. Check your internet connection or course ID");
    }

    private List<Exercise> getExercisesToDownload(Course course) {
        if (this.exercisesToDownload == null) {
            return course.getExercises();
        }
        return this.exercisesToDownload;
    }

    private Optional<String> downloadExercises(Course course) throws IOException {
        return downloadExercisesFromList(course.getExercises(), course.getName());
    }

    public Optional<String> downloadExercisesFromList(List<Exercise> exercises, String courseName) throws IOException {
        int exCount = 0;
        int totalCount = exercises.size();
        int downloaded = 0;

        String path = exerciseDownloader.createCourseFolder(data.get("path"), courseName);
        if (this.cacheFile != null) {
            cacheExercises(exercises);
        }

        for (Exercise exercise : exercises) {
            String message = exerciseDownloader.handleSingleExercise(exercise, exCount, totalCount, path);
            exCount++;
            if (!message.contains("Skip")) {
                downloaded++;
            }
            if (this.observer != null) {
                this.observer.progress(100.0 * exCount / totalCount, message);
            }
        }
        return Optional.of(downloaded + " exercises downloaded");
    }

    private void cacheExercises(List<Exercise> exercises) throws IOException {
        Gson gson = new Gson();
        String json = FileUtils.readFileToString(cacheFile, Charset.forName("UTF-8"));
        Map<Integer, String> checksums;
        if (json != null && ! json.isEmpty()) {
            Type typeOfHashMap = new TypeToken<Map<Integer, String>>() { }.getType();
            checksums = gson.fromJson(json, typeOfHashMap);
        } else {
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
