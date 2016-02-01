package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.ExerciseDownloader;
import fi.helsinki.cs.tmc.core.communication.TmcApi;
import fi.helsinki.cs.tmc.core.communication.UrlCommunicator;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;

import com.google.common.base.Optional;
import com.google.common.base.Strings;

import java.nio.file.Path;

public class DownloadModelSolution extends Command<Boolean> {

    private final Exercise exercise;
    private ExerciseDownloader exerciseDownloader;

    public DownloadModelSolution(TmcSettings settings, Exercise exercise) {
        this.settings = settings;
        this.exercise = exercise;
        // TODO: inline to the exdownloader
        TmcApi tmcApi = new TmcApi(settings);
        this.exerciseDownloader = new ExerciseDownloader(new UrlCommunicator(settings), tmcApi);
    }

    public DownloadModelSolution(
            TmcSettings settings, Exercise exercise, ExerciseDownloader downloader) {
        this.settings = settings;
        this.exercise = exercise;
        this.exerciseDownloader = downloader;
    }

    @Override
    public Boolean call() throws Exception {
        if (!settings.userDataExists()) {
            throw new TmcCoreException(
                    "Unable to download model solution: missing username/password");
        }

        String courseName = getCourseName();

        Path target =
                exerciseDownloader.createCourseFolder(settings.getTmcMainDirectory(), courseName);
        return exerciseDownloader.downloadModelSolution(exercise, target);
    }

    private String getCourseName() throws TmcCoreException {
        String courseName = exercise.getCourseName();

        if (Strings.isNullOrEmpty(courseName)) {
            Course course = resolveCurrentCourse();
            courseName = course.getName();
            exercise.setCourseName(courseName);
        }

        return courseName;
    }

    private Course resolveCurrentCourse() throws TmcCoreException {
        Optional<Course> courseOpt = settings.getCurrentCourse();
        if (!courseOpt.isPresent()) {
            throw new TmcCoreException(
                    "Could not determine course name for exercise "
                            + exercise.getName()
                            + ", course not set");
        }
        return courseOpt.get();
    }
}
