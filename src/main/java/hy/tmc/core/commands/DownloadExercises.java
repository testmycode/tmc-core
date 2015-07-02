package hy.tmc.core.commands;

import com.google.common.base.Optional;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import hy.tmc.core.communication.ExerciseDownloader;
import hy.tmc.core.communication.TmcJsonParser;
import hy.tmc.core.configuration.ClientData;
import hy.tmc.core.domain.Course;
import hy.tmc.core.domain.Exercise;
import hy.tmc.core.exceptions.TmcCoreException;
import java.io.File;
import java.io.FileWriter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;

public class DownloadExercises extends Command<String> {

    /**
     * ExerciseDownloader that is used for downloading.
     */
    private ExerciseDownloader exerciseDownloader;
    private File cacheFile;

    public DownloadExercises() {
        this.exerciseDownloader = new ExerciseDownloader();
    }

    public DownloadExercises(String path, String courseId) {
        this();
        this.setParameter("path", path);
        this.setParameter("courseID", courseId);
    }

    public DownloadExercises(String path, String courseId, File cacheFile) throws IOException {
        this(path, courseId);
        this.cacheFile = cacheFile;
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
        if (!ClientData.userDataExists()) {
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
        } catch (NumberFormatException e) {
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

        Optional<Course> courseResult = TmcJsonParser.getCourse(Integer.parseInt(this.data.get("courseID")));

        if (courseResult.isPresent()) {
            Optional<String> downloadFiles = downloadExercises(courseResult.get());
            if (downloadFiles.isPresent()) {
                return downloadFiles.get();
            }
        }
        throw new TmcCoreException("Failed to fetch exercises. Check your internet connection or course ID");
    }

    private Optional<String> downloadExercises(Course course) throws IOException {
        int exCount = 0;
        int totalCount = course.getExercises().size();
        int downloaded = 0;
        String path = exerciseDownloader.createCourseFolder(data.get("path"), course.getName());
        List<Exercise> exercises = course.getExercises();
        if (this.cacheFile != null) {
            cacheExercise(exercises);
        }
        for (Exercise exercise : exercises) {

            String message = exerciseDownloader.handleSingleExercise(exercise, exCount, totalCount, path);
            exCount++;
            if (!message.contains("Skip")) {
                downloaded++;
            }
            this.observer.progress(100.0 * exCount / totalCount, message);
        }
        return Optional.of(downloaded + " exercises downloaded");
    }

    private void cacheExercise(List<Exercise> exercises) throws IOException {
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
        
        FileWriter writer = new FileWriter(this.cacheFile);
        writer.write(gson.toJson(checksums));
    }
}
