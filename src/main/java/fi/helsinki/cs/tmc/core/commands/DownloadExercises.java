package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.cache.ExerciseChecksumCache;
import fi.helsinki.cs.tmc.core.cache.helper.CourseByIdCacheHelper;
import fi.helsinki.cs.tmc.core.communication.ExerciseDownloader;
import fi.helsinki.cs.tmc.core.communication.TmcApi;
import fi.helsinki.cs.tmc.core.communication.UrlCommunicator;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.ExerciseIdentifier;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;

import com.google.common.base.Optional;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * A {@link Command} for downloading exercises.
 */
public class DownloadExercises extends Command<List<Exercise>> {

    private ExerciseDownloader exerciseDownloader;
    private ExerciseChecksumCache exerciseChecksumCache;
    private List<Exercise> exercises;
    private int courseId;
    private Path downloadsRoot;
    private CourseByIdCacheHelper courseCacheHelper;

    /**
     *  Constructs a new downloaded exercises command for downloading {@code exercises} into TMC
     *  main directory.
     *
     * @param settings      Provides login credentials and download location.
     * @param exercises     List of exercises to download.
     * @param observer      This observer is notified of command's progress. May be {@code null}.
     * @param exerciseChecksumCache         A cache for storing the downloads.
     */
    public DownloadExercises(
            TmcSettings settings,
            List<Exercise> exercises,
            ProgressObserver observer,
            ExerciseChecksumCache exerciseChecksumCache,
            CourseByIdCacheHelper courseCacheHelper)
            throws TmcCoreException {
        super(settings, observer);

        this.exerciseDownloader = new ExerciseDownloader(new UrlCommunicator(settings), new TmcApi(settings));
        this.exercises = exercises;
        this.downloadsRoot = Paths.get(settings.getTmcMainDirectory());
        this.courseCacheHelper = courseCacheHelper;

        Optional<Course> currentCourse = settings.getCurrentCourse();
        if (currentCourse.isPresent()) {
            this.courseId = currentCourse.get().getId();
        } else {
            throw new TmcCoreException("Unable to determine course, cannot download");
        }

        this.exerciseChecksumCache = exerciseChecksumCache;
    }

    /**
     * Constructs a new downloaded exercises command for downloading exercises of the course
     * identified by {@code courseId} into {@code downloadsRoot}.
     *
     * @param settings      Provides login credentials and download location.
     * @param downloadsRoot          Target path for downloads.
     * @param courseId      Identifies which course's exercises should be downloaded.
     * @param observer      This observer is notified of command's progress. May be {@code null}.
     * @param exerciseChecksumCache         A cache for storing the downloads.
     */
    public DownloadExercises(
            TmcSettings settings,
            Path downloadsRoot,
            int courseId,
            ProgressObserver observer,
            ExerciseChecksumCache exerciseChecksumCache,
            CourseByIdCacheHelper courseCacheHelper) {
        super(settings, observer);

        this.downloadsRoot = downloadsRoot;
        this.courseId = courseId;
        this.exerciseDownloader = new ExerciseDownloader(new UrlCommunicator(settings), new TmcApi(settings));
        this.exerciseChecksumCache = exerciseChecksumCache;
        this.courseCacheHelper = courseCacheHelper;
    }

    /**
     * Constructs a new download exercises command for downloading exercises of the course
     * identified by {@code courseId} into {@code downloadsRoot}.
     *
     * @param settings      Provides login credentials and download location.
     * @param downloadsRoot          Target path for downloads.
     * @param courseId      Identifies which course's exercises should be downloaded.
     * @param exerciseChecksumCache         A cache for storing the downloads.
     * @param observer      This observer is notified of command's progress. May be {@code null}.
     * @param downloader    Downloader to download the the exercises with.
     */
    public DownloadExercises(
            TmcSettings settings,
            Path downloadsRoot,
            int courseId,
            ExerciseChecksumCache exerciseChecksumCache,
            CourseByIdCacheHelper courseCacheHelper,
            ProgressObserver observer,
            ExerciseDownloader downloader) {
        super(settings, observer);

        this.exerciseDownloader = downloader;
        this.courseId = courseId;
        this.downloadsRoot = downloadsRoot;
        this.exerciseChecksumCache = exerciseChecksumCache;
        this.courseCacheHelper = courseCacheHelper;
    }

    /**
     * Entry point for launching this command.
     */
    @Override
    public List<Exercise> call() throws TmcCoreException {
        if (!settings.userDataExists()) {
            throw new TmcCoreException("Unable to download exercises: missing username/password");
        }

        checkInterrupt();

        Course course = courseCacheHelper.get(courseId);

        if (exercises == null) {
            exercises = course.getExercises();
        }

        List<Exercise> downloadedExercises = downloadExercises(course);

        return downloadedExercises;
    }

    private List<Exercise> downloadExercises(Course course) throws TmcCoreException {
        Path target = Paths.get(exerciseDownloader.createCourseFolder(downloadsRoot.toString(), course.getName()));
        List<Exercise> downloaded = new ArrayList<>();

        for (int i = 0; i < exercises.size(); i++) {
            checkInterrupt();

            Exercise exercise = exercises.get(i);
            exercise.setCourseName(course.getName());


            boolean success = exerciseDownloader.handleSingleExercise(exercise, target.toString());

            String message = "Downloading exercise " + exercise.getName() + " failed";
            if (success) {
                downloaded.add(exercise);
                cacheLocalExercise(exercise);
                message = "Downloading exercise " + exercise.getName() + " was successful";
            }

            informObserver(i, exercises.size(), message);
        }

        return downloaded;
    }

    private void cacheLocalExercise(Exercise exercise) throws TmcCoreException {
        ExerciseIdentifier id = new ExerciseIdentifier(exercise.getCourseName(), exercise.getName());
        try {
            exerciseChecksumCache.put(id, exercise.getChecksum());
        } catch (IOException e) {
            throw new TmcCoreException("Failed to cache downloaded exercise");
        }
    }
}
