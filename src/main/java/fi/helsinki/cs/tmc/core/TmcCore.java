package fi.helsinki.cs.tmc.core;

import static com.google.common.base.Strings.isNullOrEmpty;

import fi.helsinki.cs.tmc.core.cache.ExerciseChecksumFileCache;
import fi.helsinki.cs.tmc.core.commands.DownloadExercises;
import fi.helsinki.cs.tmc.core.commands.DownloadModelSolution;
import fi.helsinki.cs.tmc.core.commands.GetCourse;
import fi.helsinki.cs.tmc.core.commands.GetExerciseUpdates;
import fi.helsinki.cs.tmc.core.commands.GetUnreadReviews;
import fi.helsinki.cs.tmc.core.commands.ListCourses;
import fi.helsinki.cs.tmc.core.commands.PasteWithComment;
import fi.helsinki.cs.tmc.core.commands.RunCheckStyle;
import fi.helsinki.cs.tmc.core.commands.RunTests;
import fi.helsinki.cs.tmc.core.commands.SendFeedback;
import fi.helsinki.cs.tmc.core.commands.SendSpywareDiffs;
import fi.helsinki.cs.tmc.core.commands.Submit;
import fi.helsinki.cs.tmc.core.commands.VerifyCredentials;
import fi.helsinki.cs.tmc.core.communication.ExerciseSubmitter;
import fi.helsinki.cs.tmc.core.communication.HttpResult;
import fi.helsinki.cs.tmc.core.communication.SubmissionPoller;
import fi.helsinki.cs.tmc.core.communication.TmcApi;
import fi.helsinki.cs.tmc.core.communication.UrlCommunicator;
import fi.helsinki.cs.tmc.core.communication.updates.ExerciseUpdateHandler;
import fi.helsinki.cs.tmc.core.communication.updates.ReviewHandler;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.domain.Review;
import fi.helsinki.cs.tmc.core.domain.submission.SubmissionResult;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;
import fi.helsinki.cs.tmc.core.spyware.DiffSender;
import fi.helsinki.cs.tmc.core.zipping.ProjectRootFinder;
import fi.helsinki.cs.tmc.langs.abstraction.ValidationResult;
import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.io.EverythingIsStudentFileStudentFilePolicy;
import fi.helsinki.cs.tmc.langs.io.zip.StudentFileAwareZipper;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    public TmcCore(TmcSettings settings,
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
            updateCache = new ExerciseChecksumFileCache(Paths.get(newCache.toString()));
        } else {
            updateCache.moveCache(Paths.get(newCache.toString()));
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
        checkParameters(
                settings.getUsername(), settings.getPassword(), settings.getServerAddress());
        VerifyCredentials login = new VerifyCredentials(settings, new UrlCommunicator(settings));
        return threadPool.submit(login);
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
        checkParameters(settings.getUsername(), settings.getPassword());
        GetCourse getter = new GetCourse(settings, url);
        return threadPool.submit(getter);
    }

    /**
     * Returns course instance with given name.
     */
     public ListenableFuture<Course> getCourse(String courseName) throws TmcCoreException {
        checkParameters(settings.getUsername(), settings.getPassword());
        GetCourse getC = new GetCourse(settings, courseName);
        return threadPool.submit(getC);
    }

    /**
     * Returns course instance with given name.
     *
     * @deprecated Use {@link #getCourse(String)} instead.
     */
    @Deprecated
    public ListenableFuture<Course> getCourseByName(String courseName)
    throws TmcCoreException {
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
     * exercises. Observer can print progress status to end-user.
     * @throws TmcCoreException if something in the given input was wrong
     */
    public ListenableFuture<List<Exercise>> downloadExercises(
            Path path, int courseId, ProgressObserver observer)
            throws TmcCoreException {
        DownloadExercises downloadCommand
                = new DownloadExercises(settings, path.toString(), courseId, observer, updateCache);
        return threadPool.submit(downloadCommand);
    }

    /**
     * Downloads exercise files specified in the given list. The exercises will be located in
     * TmcMainDirectory (field in TmcSettings).
     *
     * @param exercises to be downloaded
     */
    public ListenableFuture<List<Exercise>> downloadExercises(List<Exercise> exercises)
            throws TmcCoreException {
        return this.downloadExercises(exercises, null);
    }

    /**
     * Downloads exercises.
     */
    public ListenableFuture<List<Exercise>> downloadExercises(
            List<Exercise> exercises, ProgressObserver observer)
            throws TmcCoreException {
        DownloadExercises downloadCommand
                = new DownloadExercises(settings, exercises, observer, updateCache);
        return threadPool.submit(downloadCommand);
    }

    public ListenableFuture<Boolean> downloadModelSolution(Exercise exercise) throws TmcCoreException {
        DownloadModelSolution downloadCommand = new DownloadModelSolution(settings, exercise);
        return threadPool.submit(downloadCommand);
    }

    /**
     * Gives a list of all the courses on the current server, to which the current user has access.
     * Doesn't require login.
     *
     * @return list containing course-objects parsed from JSON
     * @throws TmcCoreException if something went wrong
     */
    public ListenableFuture<List<Course>> listCourses() throws TmcCoreException {
        ListCourses listCommand = new ListCourses(settings);
        return threadPool.submit(listCommand);
    }

    /**
     * Submits an exercise in the given path to the TMC-server. Looks for a build.xml or equivalent
     * file upwards in the path to determine exercise folder. Requires login.
     *
     * @param path inside any exercise directory
     * @return SubmissionResult object containing details of the tests run on server
     * @throws TmcCoreException if there was no course in the given path, no exercise in the given
     * path, or not logged in
     */
    public ListenableFuture<SubmissionResult> submit(Path path) throws TmcCoreException {
        UrlCommunicator communicator = new UrlCommunicator(settings);
        TmcApi tmcApi = new TmcApi(communicator, settings);

        Submit submit = new Submit(
                settings,
                new ExerciseSubmitter(
                        new ProjectRootFinder(tmcApi),
                        new StudentFileAwareZipper(new EverythingIsStudentFileStudentFilePolicy()),
                        communicator,
                        tmcApi,
                        settings),
                new SubmissionPoller(tmcApi),
                path.toString());

        return threadPool.submit(submit);
    }

    /**
     * Runs tests on the specified directory. Looks for a build.xml or equivalent file upwards in
     * the path to determine exercise folder. Doesn't require login.
     *
     * @param path inside any exercise directory
     * @return RunResult object containing details of the tests run
     * @throws TmcCoreException if there was no course in the given path, or no exercise in the
     * given path
     */
    public ListenableFuture<RunResult> test(Path path) throws TmcCoreException {
        RunTests testCommand = new RunTests(settings, path.toString());
        return threadPool.submit(testCommand);
    }

    /**
     * Runs checkstyle on the specified directory. Looks for a build.xml or equivalent file upwards
     * in the path to determine exercise folder. Doesn't require login.
     *
     * @param path inside any exercise directory
     * @return ValidationResult object containing details of the checkstyle validation
     * @throws TmcCoreException if there was no course in the given path, or no exercise in the
     * given path
     */
    public ListenableFuture<ValidationResult> runCheckstyle(Path path) throws TmcCoreException {

        RunCheckStyle checkstyleCommand = new RunCheckStyle(path.toString());
        return threadPool.submit(checkstyleCommand);
    }

    /**
     * Fetches unread reviews from the TMC server. Does not mark reviews as read.
     *
     * @param course the course whose reviews are checked
     * @return a list of unread reviews for the given course
     */
    public ListenableFuture<List<Review>> getNewReviews(Course course) throws TmcCoreException {
        ReviewHandler reviewHandler = new ReviewHandler(new TmcApi(settings));
        GetUnreadReviews command = new GetUnreadReviews(course, reviewHandler);
        return threadPool.submit(command);
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
     * given path
     */
    public ListenableFuture<List<Exercise>> getNewAndUpdatedExercises(Course course)
            throws TmcCoreException {
        ExerciseUpdateHandler updater
                = new ExerciseUpdateHandler(updateCache, new TmcApi(settings));
        GetExerciseUpdates command = new GetExerciseUpdates(course, updater);
        return threadPool.submit(command);
    }

    /**
     * Sends feedback answers to the TMC server.
     *
     * @param answers map of question_id -> answer
     * @param url url that the answers will be sent to
     * @return a HttpResult of the servers reply. It should contain "{status:ok}" if everything goes
     * well
     */
    public ListenableFuture<HttpResult> sendFeedback(Map<String, String> answers, URI url)
            throws TmcCoreException {
        SendFeedback feedback = new SendFeedback(settings, answers, url.toString());
        return threadPool.submit(feedback);
    }

    /**
     * Submits the current exercise to the TMC-server and requests for a paste to be made, with
     * comment given by user.
     *
     * @param path inside any exercise directory
     * @param comment comment given by user
     * @return URI object containing location of the paste
     * @throws TmcCoreException if there was no course in the given path, or no exercise in the
     * given path
     */
    public ListenableFuture<URI> pasteWithComment(Path path, String comment)
            throws TmcCoreException {
        //checkParameters(path);
        PasteWithComment paste = new PasteWithComment(settings, path.toString(), comment);
        return threadPool.submit(paste);
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
        SendSpywareDiffs spyware
                = new SendSpywareDiffs(settings, new DiffSender(settings), spywareDiffs);
        return threadPool.submit(spyware);
    }

    private void checkParameters(String... params) throws TmcCoreException {
        for (String param : params) {
            if (isNullOrEmpty(param)) {
                throw new TmcCoreException("Param empty or null.");
            }
        }
    }
}
