package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.cache.ExerciseChecksumCache;
import fi.helsinki.cs.tmc.core.communication.ExerciseDownloader;
import fi.helsinki.cs.tmc.core.communication.ExerciseObserver;
import fi.helsinki.cs.tmc.core.communication.TmcApi;
import fi.helsinki.cs.tmc.core.communication.UrlCommunicator;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;
import fi.helsinki.cs.tmc.core.exceptions.TmcInterruptionException;

import com.google.common.base.Optional;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A {@link Command} for downloading exercises.
 */
public class DownloadExercises extends Command<List<Exercise>> {

    private ExerciseDownloader exerciseDownloader;
    private ExerciseChecksumCache cache;
    private TmcApi tmcApi;
    private List<Exercise> exercises;
    private int courseId;
    private Path path;

    /**
     * Constructs a new downloaded exercises command for downloading
     * {@code exercises} into TMC main directory.
     *
     * @param settings provides login credentials and download location
     * @param exercises list of exercises to download
     * @param observer this observer is notified of command's progress. May be
     * {@code null}
     * @param cache a cache for storing the downloads
     */
    public DownloadExercises(
            TmcSettings settings,
            List<Exercise> exercises,
            ProgressObserver observer,
            ExerciseChecksumCache cache)
            throws TmcCoreException {
        super(settings, observer);

        this.tmcApi = new TmcApi(settings);
        this.exerciseDownloader = new ExerciseDownloader(new UrlCommunicator(settings), tmcApi);
        this.exercises = exercises;
        this.path = settings.getTmcMainDirectory();

        Optional<Course> currentCourse = settings.getCurrentCourse();
        if (currentCourse.isPresent()) {
            this.courseId = currentCourse.get().getId();
        } else {
            throw new TmcCoreException("Unable to determine course, cannot download");
        }

        this.cache = cache;
    }

    /**
     * Constructs a new downloaded exercises command for downloading exercises
     * of the course identified by {@code courseId} into {@code path}.
     *
     * @param settings provides login credentials and download location
     * @param path target path for downloads
     * @param courseId identifies which course's exercises should be downloaded
     * @param observer this observer is notified of command's progress. May be
     * {@code null}
     * @param cache a cache for storing the downloads
     */
    public DownloadExercises(
            TmcSettings settings,
            Path path,
            int courseId,
            ProgressObserver observer,
            ExerciseChecksumCache cache) {
        super(settings, observer);

        this.path = path;
        this.courseId = courseId;
        this.tmcApi = new TmcApi(settings);
        this.exerciseDownloader = new ExerciseDownloader(new UrlCommunicator(settings), tmcApi);
        this.cache = cache;
        this.tmcApi = new TmcApi(settings);
    }

    /**
     * Constructs a new download exercises command for downloading exercises of the course
     * identified by {@code courseId} into {@code path}.
     *
     * @param settings      provides login credentials and download location
     * @param path          target path for downloads
     * @param courseId      identifies which course's exercises should be downloaded
     * @param cache         a cache for storing the downloads
     * @param observer      this observer is notified of command's progress. May be {@code null}
     * @param downloader    downloader to download the the exercises with
     * @param tmcApi        TMC server connector for querying the server with
     */
    public DownloadExercises(
            TmcSettings settings,
            Path path,
            int courseId,
            ExerciseChecksumCache cache,
            ProgressObserver observer,
            ExerciseDownloader downloader,
            TmcApi tmcApi) {
        super(settings, observer);

        this.exerciseDownloader = downloader;
        this.courseId = courseId;
        this.path = path;
        this.cache = cache;
        this.tmcApi = tmcApi;
    }

    /**
     * Entry point for launching this command.
     */
    @Override
    public List<Exercise> call() throws TmcCoreException, IOException {
        if (!settings.userDataExists()) {
            throw new TmcCoreException("Unable to download exercises: missing username/password");
        }

        checkInterrupt();

        Course course = getCourse();
        if (exercises == null) {
            exercises = course.getExercises();
        }

        List<Exercise> downloadedExercises = downloadExercises(course);

        checkCache();

        return downloadedExercises;
    }

    private void checkCache() throws TmcCoreException {
        if (cache != null) {
            try {
                cache.write(exercises);
            } catch (IOException e) {
                throw new TmcCoreException("Unable to write exercise checksums to cache", e);
            }
        }
    }

    private List<Exercise> downloadExercises(final Course course)
            throws TmcInterruptionException, IOException {

        final List<Exercise> downloaded = new ArrayList<>();
        final AtomicInteger counter = new AtomicInteger();

        ExerciseObserver exerciseObserver = createExerciseObserver(course, downloaded, counter);
        exerciseDownloader.downloadExercises(exercises, this.path, course.getName(),
                exerciseObserver);
        return downloaded;
    }

    private ExerciseObserver createExerciseObserver(final Course course, final List<Exercise> downloaded, final AtomicInteger counter) {
        return new ExerciseObserver() {
                @Override
                public void observe(Exercise exercise, boolean success) {
                    exercise.setCourseName(course.getName());

                    String message = "Downloading exercise " + exercise.getName() + " failed";
                    if (success) {
                        downloaded.add(exercise);
                        message = "Downloading exercise " + exercise.getName() + " was successful";
                    }

                    informObserver(counter.incrementAndGet(), exercises.size(), message);
                }
            };
    }

    private Course getCourse() throws TmcCoreException {
        try {
            Optional<Course> courseResult = this.tmcApi.getCourse(this.courseId);

            if (!courseResult.isPresent()) {
                throw new TmcCoreException(
                        "Unable to download exercises: unable to identify course. ");
            }

            return courseResult.get();

        } catch (IOException | URISyntaxException ex) {
            throw new TmcCoreException(
                    "Unable to download exercises: unable to get course details", ex);
        }
    }
}
