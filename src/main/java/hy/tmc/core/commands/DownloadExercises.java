package hy.tmc.core.commands;

import com.google.common.base.Optional;
import hy.tmc.core.communication.ExerciseDownloader;
import hy.tmc.core.communication.TmcJsonParser;
import hy.tmc.core.configuration.ClientData;
import hy.tmc.core.domain.Course;
import hy.tmc.core.domain.Exercise;
import hy.tmc.core.exceptions.ProtocolException;

import java.io.IOException;
import java.util.List;

public class DownloadExercises extends Command<String> {

    /**
     * ExerciseDownloader that is used for downloading.
     */
    private ExerciseDownloader exerciseDownloader;

    public DownloadExercises() {
        this.exerciseDownloader = new ExerciseDownloader();
    }

    public DownloadExercises(String path, String courseId) {
        this();
        this.setParameter("path", path);
        this.setParameter("courseID", courseId);
    }

    /**
     * Checks that command has required parameters courseID is the id of the course and path is the
     * path of where files are downloaded and extracted.
     *
     * @throws ProtocolException if path isn't supplied
     */
    @Override
    public void checkData() throws ProtocolException {
        checkCourseId();
        if (!this.data.containsKey("path")) {
            throw new ProtocolException("Path required");
        }
        if (!ClientData.userDataExists()) {
            throw new ProtocolException("You need to login first.");
        }
    }

    /**
     * Check that user has given also course id.
     *
     * @throws ProtocolException if course id is not a number
     */
    private void checkCourseId() throws ProtocolException {
        if (!this.data.containsKey("courseID")) {
            throw new ProtocolException("Course ID required");
        }
        try {
            int courseId = Integer.parseInt(this.data.get("courseID"));
        }
        catch (NumberFormatException e) {
            throw new ProtocolException("Given course id is not a number");
        }
    }

    /**
     * Parses the course JSON and executes downloading of the course exercises.
     *
     * @return
     */
    @Override
    public String call() throws ProtocolException, IOException {
        checkData();

        Optional<Course> courseResult = TmcJsonParser.getCourse(Integer.parseInt(this.data.get("courseID")));

        if (courseResult.isPresent()) {
            Optional<String> downloadFiles = downloadExercises(courseResult.get());
            if (downloadFiles.isPresent()) {
                return downloadFiles.get();
            }
        }
        throw new ProtocolException("Failed to fetch exercises. Check your internet connection or course ID");
    }

    private Optional<String> downloadExercises(Course course) {
        return downloadExercisesFromList(course.getExercises(), course.getName());
    }
    
    public Optional<String> downloadExercisesFromList(List<Exercise> exercises, String courseName){
        int exCount = 0;
        int totalCount = exercises.size();
        int downloaded = 0;
        String path = exerciseDownloader.createCourseFolder(data.get("path"), courseName);
        for (Exercise exercise : exercises) {
            String message = exerciseDownloader.handleSingleExercise(exercise, exCount, totalCount, path);
            exCount++;
            if (!message.contains("Skip")) {
                downloaded++;
            }
            this.observer.progress(100.0*exCount/totalCount, message);
        }
        return Optional.of(downloaded+" exercises downloaded");
    }
}
