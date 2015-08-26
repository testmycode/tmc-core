package fi.helsinki.cs.tmc.core.commands;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import fi.helsinki.cs.tmc.core.communication.ExerciseDownloader;
import fi.helsinki.cs.tmc.core.communication.TmcApi;
import fi.helsinki.cs.tmc.core.communication.UrlCommunicator;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DownloadModelSolution extends Command<Boolean> {

    private final Exercise exercise;
    private ExerciseDownloader exerciseDownloader;

    public DownloadModelSolution(TmcSettings settings, Exercise exercise) {
        this.settings = settings;
        this.exercise = exercise;
        TmcApi tmcApi = new TmcApi(settings);
        this.exerciseDownloader = new ExerciseDownloader(new UrlCommunicator(settings), tmcApi);
    }
    
    public DownloadModelSolution(TmcSettings settings, Exercise exercise, ExerciseDownloader downloader) {
        this.settings = settings;
        this.exercise = exercise;
        this.exerciseDownloader = downloader;
    }

    @Override
    public Boolean call() throws Exception {
        if (!settings.userDataExists()) {
            throw new TmcCoreException("Unable to download model solution: missing username/password");
        }

        String courseName = getCourseName();

        Path target = Paths.get(exerciseDownloader.createCourseFolder(exercise.getName(), courseName));
        
        return exerciseDownloader.downloadModelSolution(exercise, target);
    }
    
    private String getCourseName() throws TmcCoreException {
        String courseName = exercise.getCourseName();

        if (Strings.isNullOrEmpty(courseName)) {
            Optional<Course> courseOpt = settings.getCurrentCourse();
            if (!courseOpt.isPresent()) {
                throw new TmcCoreException("Could not determine course name for exercise " 
                        + exercise.getName() + ", course not set");
            }
            Course course = courseOpt.get();
            courseName = course.getName();
            exercise.setCourseName(courseName);
        }
        
        return courseName;
    }

}
