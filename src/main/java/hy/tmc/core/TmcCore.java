package hy.tmc.core;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.stylerunner.validation.ValidationResult;
import hy.tmc.core.communication.HttpResult;
import hy.tmc.core.commands.VerifyCredentials;
import hy.tmc.core.commands.DownloadExercises;
import hy.tmc.core.commands.GetCourse;
import hy.tmc.core.commands.GetExerciseUpdates;
import hy.tmc.core.commands.GetUnreadReviews;
import hy.tmc.core.commands.ListCourses;
import hy.tmc.core.commands.PasteWithComment;
import hy.tmc.core.commands.RunCheckStyle;
import hy.tmc.core.commands.RunTests;
import hy.tmc.core.commands.SendFeedback;
import hy.tmc.core.commands.SendSpywareDiffs;
import hy.tmc.core.commands.Submit;
import hy.tmc.core.communication.TmcJsonParser;
import hy.tmc.core.communication.updates.ExerciseUpdateHandler;
import hy.tmc.core.communication.updates.ReviewHandler;
import hy.tmc.core.configuration.TmcSettings;
import hy.tmc.core.domain.Course;
import hy.tmc.core.domain.Exercise;
import hy.tmc.core.domain.ProgressObserver;
import hy.tmc.core.domain.Review;
import hy.tmc.core.domain.submission.SubmissionResult;
import hy.tmc.core.exceptions.TmcCoreException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import static com.google.common.base.Strings.isNullOrEmpty;

public class TmcCore {

    static ListeningExecutorService threadPool = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
    private File updateCache;

    /**
     * The TmcCore that can be used as a standalone businesslogic for any tmc client application.
     * The TmcCore provides all the essential backend functionalities as public methods.
     */
    public TmcCore() {
        threadPool = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
    }

    public TmcCore(File updateCache) throws FileNotFoundException {
        this(updateCache, MoreExecutors.listeningDecorator(Executors.newCachedThreadPool()));
    }

    public TmcCore(File updateCache, ListeningExecutorService threadPool) throws FileNotFoundException {
        this(threadPool);
        ensureCacheFileExists(updateCache);
        this.updateCache = updateCache;
    }

    public void setCacheFile(File newCache) throws IOException, TmcCoreException {
        ensureCacheFileExists(newCache);
        if (this.updateCache != null && this.updateCache.exists()) {
            moveCacheFile(newCache);
        }
        updateCache = newCache;
    }

    private void moveCacheFile(File newCache) throws IOException, TmcCoreException {
        String oldData = FileUtils.readFileToString(updateCache, Charset.forName("UTF-8"));
        FileWriter writer = new FileWriter(newCache, true);
        writer.write(oldData);
        writer.close();
        File old = updateCache;
        updateCache = newCache;
        old.delete();
    }

    public File getCacheFile() {
        return this.updateCache;
    }

    private void ensureCacheFileExists(File cacheFile) throws FileNotFoundException {
        if (cacheFile == null) {
            throw new FileNotFoundException("Cannot find file: null");
        }
        if (!cacheFile.exists()) {
            String errorMessage = "cache file " + cacheFile.getAbsolutePath() + " does not exist";
            throw new FileNotFoundException(errorMessage);
        }
    }

    /**
     * For dependency injection of threadpool.
     *
     * @param pool thread threadpool which to use with the core
     */
    public TmcCore(ListeningExecutorService pool) {
        this.threadPool = pool;
    }

    /**
     * Authenticates the given user on the server.
     *
     * @param settings containing credentials and server address.
     * @return A future-object containing true or false on success or fail
     */
    public ListenableFuture<Boolean> verifyCredentials(TmcSettings settings) throws TmcCoreException {
        checkParameters(settings.getUsername(), settings.getPassword(), settings.getServerAddress());
        VerifyCredentials login = new VerifyCredentials(settings);
        return threadPool.submit(login);
    }

    /**
     * Fetch one course from tmc-server.
     *
     * @param settings containing at least credentials
     * @param url defines the url to course
     */
    public ListenableFuture<Course> getCourse(TmcSettings settings, String url) throws TmcCoreException {
        try {
            checkParameters(settings.getUsername(), settings.getPassword());
            GetCourse getter = new GetCourse(settings, new URI(url));
            return threadPool.submit(getter);
        }
        catch (URISyntaxException ex) {
            throw new TmcCoreException("Invalid url", ex);
        }
    }

    public ListenableFuture<Course> getCourseByName(TmcSettings settings, String courseName) throws TmcCoreException, IOException {
        checkParameters(settings.getUsername(), settings.getPassword());
        GetCourse getC = new GetCourse(settings, courseName);
        return threadPool.submit(getC);
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
    public ListenableFuture<List<Exercise>> downloadExercises(String path, String courseId,
            TmcSettings settings, ProgressObserver observer) throws TmcCoreException {
        checkParameters(path, courseId);
        DownloadExercises downloadCommand = getDownloadCommand(path, courseId, settings, observer);
        return threadPool.submit(downloadCommand);
    }

    /**
     * Downloads exercise files specified in the given list. The exercises will be located in
     * TmcMainDirectory (field in TmcSettings).
     *
     * @param exercises to be downloaded
     * @param settings object that implements TmcSettings-interface. Requisite fields are username,
     * password, serverAddress and TmcMainDirectory.
     */
    public ListenableFuture<List<Exercise>> downloadExercises(List<Exercise> exercises, TmcSettings settings) throws TmcCoreException {
        checkParameters(settings.getFormattedUserData(), settings.getTmcMainDirectory(),
                settings.getServerAddress());
        DownloadExercises downloadCommand;
        if (this.updateCache != null) {
            downloadCommand = new DownloadExercises(exercises, settings, updateCache);
        } else {
            downloadCommand = new DownloadExercises(exercises, settings);
        }
        return threadPool.submit(downloadCommand);
    }

    private DownloadExercises getDownloadCommand(String path, String courseId,
            TmcSettings settings, ProgressObserver observer) {
        if (this.updateCache == null) {
            return new DownloadExercises(path, courseId, settings, observer);
        }
        return new DownloadExercises(path, courseId, settings, this.updateCache, observer);
    }

    private DownloadExercises getDownloadCommand(List<Exercise> exercises, TmcSettings settings, ProgressObserver observer)
            throws TmcCoreException {
        if (observer == null) {
            if (this.updateCache == null) {
                return new DownloadExercises(exercises, settings);
            }
            return new DownloadExercises(exercises, settings, this.updateCache);
        }
        if (this.updateCache == null) {
            return new DownloadExercises(exercises, settings, observer);
        }
        return new DownloadExercises(exercises, settings, this.updateCache, observer);
    }

    /**
     * Gives a list of all the courses on the current server, to which the current user has access.
     * Doesn't require login.
     *
     * @return list containing course-objects parsed from JSON
     * @throws TmcCoreException if something went wrong
     */
    public ListenableFuture<List<Course>> listCourses(TmcSettings settings) throws TmcCoreException {
        @SuppressWarnings("unchecked")
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
    public ListenableFuture<SubmissionResult> submit(String path, TmcSettings settings) throws TmcCoreException {
        checkParameters(path);
        Submit submit = new Submit(path, settings);
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
    public ListenableFuture<RunResult> test(String path, TmcSettings settings) throws TmcCoreException {
        checkParameters(path);
        @SuppressWarnings("unchecked")
        RunTests testCommand = new RunTests(path, settings);
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
    public ListenableFuture<ValidationResult> runCheckstyle(String path, TmcSettings settings) throws TmcCoreException {
        checkParameters(path);
        @SuppressWarnings("unchecked")
        RunCheckStyle checkstyleCommand = new RunCheckStyle(path, settings);
        return threadPool.submit(checkstyleCommand);
    }

    /**
     * Fetches unread reviews from the TMC server. Does not mark reviews as read.
     *
     * @param course the course whose reviews are checked
     * @return a list of unread reviews for the given course
     */
    public ListenableFuture<List<Review>> getNewReviews(Course course, TmcSettings settings) throws TmcCoreException {
        ReviewHandler reviewHandler = new ReviewHandler(new TmcJsonParser(settings));
        GetUnreadReviews command = new GetUnreadReviews(course, reviewHandler, settings);
        command.checkData();
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
    public ListenableFuture<List<Exercise>> getNewAndUpdatedExercises(Course course, TmcSettings settings) throws TmcCoreException, IOException {
        ExerciseUpdateHandler updater = new ExerciseUpdateHandler(updateCache, new TmcJsonParser(settings));
        GetExerciseUpdates command = new GetExerciseUpdates(course, updater, settings);
        command.checkData();
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
    public ListenableFuture<HttpResult> sendFeedback(Map<String, String> answers, String url, TmcSettings settings) throws TmcCoreException, IOException {
        SendFeedback feedback = new SendFeedback(answers, url, settings);
        feedback.checkData();
        return threadPool.submit(feedback);
    }

    /**
     * Submits the current exercise to the TMC-server and requests for a paste to be made, with
     * comment given by user.
     *
     * @param path inside any exercise directory
     * @param comment, comment given by user
     * @return URI object containing location of the paste
     * @throws TmcCoreException if there was no course in the given path, or no exercise in the
     * given path
     */
    public ListenableFuture<URI> pasteWithComment(String path, TmcSettings settings, String comment) throws TmcCoreException {
        checkParameters(path);
        PasteWithComment paste = new PasteWithComment(path, settings, comment);
        return threadPool.submit(paste);
    }

    /**
     * Sends given diffs to spyware server. Server is specified in current course that can be found
     * from TmcSettings.
     *
     * @param spywareDiffs byte array containing information of changes to project files.
     * @return A future object containing a results from every spyware server.
     */
    public ListenableFuture<List<HttpResult>> sendSpywareDiffs(byte[] spywareDiffs, TmcSettings settings) throws TmcCoreException {
        SendSpywareDiffs spyware = new SendSpywareDiffs(spywareDiffs, settings);
        spyware.checkData();
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
