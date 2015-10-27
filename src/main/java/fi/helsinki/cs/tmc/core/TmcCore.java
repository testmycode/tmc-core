package fi.helsinki.cs.tmc.core;


import static fi.helsinki.cs.tmc.core.commands.factory.CommandFactory.getCourseCmd;
import static fi.helsinki.cs.tmc.core.commands.factory.CommandFactory.getDownloadExercisesCmd;
import static fi.helsinki.cs.tmc.core.commands.factory.CommandFactory.getDownloadModelSolutionCmd;
import static fi.helsinki.cs.tmc.core.commands.factory.CommandFactory.getExerciseUpdatesCmd;
import static fi.helsinki.cs.tmc.core.commands.factory.CommandFactory.getListCoursesCmd;
import static fi.helsinki.cs.tmc.core.commands.factory.CommandFactory.getPasteWithCommentCmd;
import static fi.helsinki.cs.tmc.core.commands.factory.CommandFactory.getRequestCodeReviewCmd;
import static fi.helsinki.cs.tmc.core.commands.factory.CommandFactory.getRunCheckStyleCmd;
import static fi.helsinki.cs.tmc.core.commands.factory.CommandFactory.getRunTestsCmd;
import static fi.helsinki.cs.tmc.core.commands.factory.CommandFactory.getSendFeedbackCmd;
import static fi.helsinki.cs.tmc.core.commands.factory.CommandFactory.getSendSpywareDiffsCmd;
import static fi.helsinki.cs.tmc.core.commands.factory.CommandFactory.getSubmitCmd;
import static fi.helsinki.cs.tmc.core.commands.factory.CommandFactory.getUnreadReviewsCmd;
import static fi.helsinki.cs.tmc.core.commands.factory.CommandFactory.getVerifyCredentialsCmd;
import static fi.helsinki.cs.tmc.core.util.ParameterTester.checkStringParameters;

import fi.helsinki.cs.tmc.core.cache.ExerciseChecksumFileCache;
import fi.helsinki.cs.tmc.core.commands.Command;
import fi.helsinki.cs.tmc.core.communication.HttpResult;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.domain.Review;
import fi.helsinki.cs.tmc.core.domain.submission.SubmissionResult;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;
import fi.helsinki.cs.tmc.langs.abstraction.ValidationResult;
import fi.helsinki.cs.tmc.langs.domain.RunResult;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class TmcCore {

    private ListeningExecutorService threadPool;
    private ExerciseChecksumFileCache updateCache;
    private TmcSettings settings;

    /**
     * The TmcCore that can be used as a standalone businesslogic for any tmc client application.
     * The TmcCore provides all the essential backend functionalities as public methods.
     */
    public TmcCore(TmcSettings settings) {
        this(settings, MoreExecutors.listeningDecorator(Executors.newCachedThreadPool()));
    }

    public TmcCore(TmcSettings settings, ListeningExecutorService pool) {
        this.settings = settings;
        this.threadPool = pool;
    }

    public TmcCore(
            TmcSettings settings,
            Path exerciseChecksumCacheLocation,
            ListeningExecutorService threadPool)
            throws FileNotFoundException {
        this(settings, threadPool);

        this.updateCache = new ExerciseChecksumFileCache(exerciseChecksumCacheLocation);
    }

    public void setExerciseChecksumCacheLocation(Path newCache) throws IOException {
        if (newCache == null || Files.notExists(newCache)) {
            throw new FileNotFoundException("Attempted to set non-existent cache file");
        }
        if (updateCache == null) {
            updateCache = new ExerciseChecksumFileCache(newCache);
        } else {
            updateCache.moveCache(newCache);
        }
    }

    public Path getExerciseChecksumCacheLocation() {
        return this.updateCache.getCacheFile();
    }

    /**
     * Authenticates the given user on the server.
     *
     * @return A future-object containing true or false on success or fail
     */
    public ListenableFuture<Boolean> verifyCredentials() throws TmcCoreException {
        checkStringParameters(
                settings.getUsername(), settings.getPassword(), settings.getServerAddress());

        return threadPool.submit(getVerifyCredentialsCmd(settings));
    }

    /**
     * Fetch one course from tmc-server.
     *
     * @param url defines the url to course
     *
     * @deprecated Use {@link #getCourse(String)} instead.
     */
    @Deprecated
    public ListenableFuture<Course> getCourse(URI url) throws TmcCoreException {
        checkStringParameters(settings.getUsername(), settings.getPassword());
        return threadPool.submit(getCourseCmd(settings, url));
    }

    /**
     * Returns course instance with given name.
     */
    public ListenableFuture<Course> getCourse(String courseName) throws TmcCoreException {
        checkStringParameters(settings.getUsername(), settings.getPassword());
        return threadPool.submit(getCourseCmd(settings, courseName));
    }

    /**
     * Returns course instance with given name.
     *
     * @deprecated Use {@link #getCourse(String)} instead.
     */
    @Deprecated
    public ListenableFuture<Course> getCourseByName(String courseName) throws TmcCoreException {
        return getCourse(courseName);
    }

    /**
     * Downloads all exercise files of a given course (specified by id) to the given directory. If
     * files exist, overrides everything except the source folder and files specified in
     * .tmcproject.yml Requires login.
     *
     * @param path where it downloads the exercises
     * @param courseId ID of course to download
     * @param observer ProgressObserver will be informed about the progress of downloading
     *       exercises. Observer can print progress status to end-user
     * @throws TmcCoreException if something in the given input was wrong
     */
    public ListenableFuture<List<Exercise>> downloadExercises(
            Path path, int courseId, ProgressObserver observer) throws TmcCoreException {

        Command<List<Exercise>> downloadExercisesCmd =
                getDownloadExercisesCmd(settings, path, courseId, observer, updateCache);
        return threadPool.submit(downloadExercisesCmd);
    }

    public ListenableFuture<Boolean> downloadModelSolution(Exercise exercise)
            throws TmcCoreException {
        Command<Boolean> downloadModelSolutionCmd = getDownloadModelSolutionCmd(settings, exercise);
        return threadPool.submit(downloadModelSolutionCmd);
    }

    /**
     * Gives a list of all the courses on the current server, to which the current user has access.
     * Doesn't require login.
     *
     * @return list containing course-objects parsed from JSON
     * @throws TmcCoreException if something went wrong
     */
    public ListenableFuture<List<Course>> listCourses() throws TmcCoreException {
        return threadPool.submit(getListCoursesCmd(settings));
    }

    /**
     * Submits an exercise in the given path to the TMC-server. Looks for a build.xml or equivalent
     * file upwards in the path to determine exercise folder. Requires login.
     *
     * @param path inside any exercise directory
     * @return SubmissionResult object containing details of the tests run on server
     * @throws TmcCoreException if there was no course in the given path, no exercise in the given
     *       path, or not logged in.
     */
    public ListenableFuture<SubmissionResult> submit(Path path) throws TmcCoreException {
        return submit(path, null);
    }

    /**
     * Submits an exercise in the given path to the TMC-server. Looks for a build.xml or equivalent
     * file upwards in the path to determine exercise folder. Requires login.
     *
     * @param path inside any exercise directory
     * @param observer a {@link ProgressObserver} which will be informed of the submits progress
     * @return SubmissionResult object containing details of the tests run on server
     * @throws TmcCoreException if there was no course in the given path, no exercise in the given
     *       path, or not logged in.
     */
    public ListenableFuture<SubmissionResult> submit(Path path, ProgressObserver observer)
            throws TmcCoreException {

        return threadPool.submit(getSubmitCmd(settings, path, observer));
    }

    /**
     * Runs tests on the specified directory. Looks for a build.xml or equivalent file upwards in
     * the path to determine exercise folder. Doesn't require login.
     *
     * @param path inside any exercise directory
     * @return RunResult object containing details of the tests run
     * @throws TmcCoreException if there was no course in the given path, or no exercise in the
     *       given path.
     */
    public ListenableFuture<RunResult> test(Path path) throws TmcCoreException {
        return threadPool.submit(getRunTestsCmd(settings, path));
    }

    /**
     * Runs checkstyle on the specified directory. Looks for a build.xml or equivalent file upwards
     * in the path to determine exercise folder. Doesn't require login.
     *
     * @param path inside any exercise directory
     * @return ValidationResult object containing details of the checkstyle validation
     * @throws TmcCoreException if there was no course in the given path, or no exercise in the
     *       given path.
     */
    public ListenableFuture<ValidationResult> runCheckstyle(Path path) throws TmcCoreException {
        return threadPool.submit(getRunCheckStyleCmd(path));
    }

    /**
     * Fetches unread reviews from the TMC server. Does not mark reviews as read.
     *
     * @param course the course whose reviews are checked
     * @return a list of unread reviews for the given course
     */
    public ListenableFuture<List<Review>> getNewReviews(Course course) throws TmcCoreException {
        return threadPool.submit(getUnreadReviewsCmd(settings, course));
    }

    /**
     * Returns a list of exercises that have not been downloaded yet, or have newer versions on the
     * tmc server. This method will use the cache file specified by the last call to setCacheFile.
     * Updates are detected by comparing checksums. The checksums will be written to a file each
     * time exercises are downloaded, provided that setCacheFile has been called.
     *
     * @param course the course whose exercises are checked
     * @return a list of exercises that are new or have updates
     * @throws TmcCoreException if there was no course in the given path, or no exercise in the
     *       given path.
     */
    public ListenableFuture<List<Exercise>> getNewAndUpdatedExercises(Course course)
            throws TmcCoreException {

        Command<List<Exercise>> exerciseUpdatesCmd =
                getExerciseUpdatesCmd(settings, updateCache, course);
        return threadPool.submit(exerciseUpdatesCmd);
    }

    /**
     * Sends feedback answers to the TMC server.
     *
     * @param answers map of question_id -> answer
     * @param url url that the answers will be sent to
     * @return a HttpResult of the servers reply. It should contain "{status:ok}" if everything goes
     *      well.
     */
    public ListenableFuture<HttpResult> sendFeedback(Map<String, String> answers, URI url)
            throws TmcCoreException {

        return threadPool.submit(getSendFeedbackCmd(settings, answers, url));
    }

    /**
     * Submits the current exercise to the TMC-server and requests for a paste to be made, with
     * comment given by user.
     *
     * @param path inside any exercise directory
     * @param comment comment given by user
     * @return URI object containing location of the paste
     * @throws TmcCoreException if there was no course in the given path, or no exercise in the
     *       given path.
     */
    public ListenableFuture<URI> pasteWithComment(Path path, String comment)
            throws TmcCoreException {

        return threadPool.submit(getPasteWithCommentCmd(settings, path, comment));
    }

    /**
     * Submits the current exercise to the TMC-server and requests for a code review, with a
     * message given by user.
     *
     * @param path inside any exercise directory
     * @param message message given by user
     * @return URI object containing location of the submission
     * @throws TmcCoreException if there was no course in the given path, or no exercise in the
     *     given path
     */
    public ListenableFuture<URI> requestCodeReview(Path path, String message)
            throws TmcCoreException {

        return threadPool.submit(getRequestCodeReviewCmd(settings, path, message));
    }

    /**
     * Sends given diffs to spyware server. Server is specified in current course that can be found
     * from TmcSettings.
     *
     * @param spywareDiffs byte array containing information of changes to project files.
     * @return A future object containing a results from every spyware server.
     */
    public ListenableFuture<List<HttpResult>> sendSpywareDiffs(byte[] spywareDiffs)
            throws TmcCoreException {

        Command<List<HttpResult>> sendSpywareDiffsCmd =
                getSendSpywareDiffsCmd(settings, spywareDiffs);
        return threadPool.submit(sendSpywareDiffsCmd);
    }
}
