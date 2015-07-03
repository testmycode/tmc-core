package hy.tmc.core;

import static com.google.common.base.Strings.isNullOrEmpty;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import fi.helsinki.cs.tmc.langs.RunResult;
import hy.tmc.core.communication.HttpResult;
import hy.tmc.core.domain.Course;
import hy.tmc.core.domain.Exercise;
import hy.tmc.core.domain.submission.SubmissionResult;
import hy.tmc.core.commands.VerifyCredentials;
import hy.tmc.core.commands.ChooseServer;
import hy.tmc.core.commands.DownloadExercises;
import hy.tmc.core.commands.GetExerciseUpdates;
import hy.tmc.core.commands.GetUnreadReviews;
import hy.tmc.core.commands.ListCourses;
import hy.tmc.core.commands.ListExercises;
import hy.tmc.core.commands.Logout;
import hy.tmc.core.commands.Paste;
import hy.tmc.core.commands.RunTests;
import hy.tmc.core.commands.SendFeedback;
import hy.tmc.core.commands.Submit;
import hy.tmc.core.communication.updates.ExerciseUpdateHandler;
import hy.tmc.core.communication.updates.ReviewHandler;
import hy.tmc.core.configuration.ClientData;
import hy.tmc.core.domain.Credentials;
import hy.tmc.core.domain.Review;
import hy.tmc.core.exceptions.TmcCoreException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import org.apache.commons.io.FileUtils;

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

    public void setCacheFile(File newCache) throws FileNotFoundException, IOException, TmcCoreException {
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
     * Authenticates the given user on the server, and saves the data into memory.
     *
     * @param credentials includes username and password
     * @param serverAddress defines TMC-server to communicate with.
     * @return A future-object containing true or false on success or fail
     * @throws TmcCoreException if something in the given input was wrong
     */
    public ListenableFuture<Boolean> verifyCredentials(Credentials credentials, String serverAddress) throws TmcCoreException {
        checkParameters(credentials.getUsername(), credentials.getPassword(), serverAddress);
        ClientData.setServerAddress(serverAddress);
        VerifyCredentials login = new VerifyCredentials(credentials.getUsername(), credentials.getPassword());
        @SuppressWarnings("unchecked")
        ListenableFuture<Boolean> stringListenableFuture = (ListenableFuture<Boolean>) threadPool.submit(login);
        return stringListenableFuture;
    }

    /**
     * Downloads the exercise files of a given source to the given directory. If files exist,
     * overrides everything except the source folder and files specified in .tmcproject.yml Requires
     * login.
     *
     * @param path where it downloads the exercises
     * @param courseId ID of course to download
     * @return A future-object containing true or false on success or fail
     * @throws TmcCoreException if something in the given input was wrong
     */
    public ListenableFuture<String> downloadExercises(String path, String courseId) throws TmcCoreException, IOException {
        checkParameters(path, courseId);
        @SuppressWarnings("unchecked")
        DownloadExercises downloadCommand = getDownloadCommand(path, courseId);
        ListenableFuture<String> stringListenableFuture = (ListenableFuture<String>) threadPool.submit(downloadCommand);
        return stringListenableFuture;
    }

    private DownloadExercises getDownloadCommand(String path, String courseId) throws IOException {
        if (this.updateCache == null) {
            return new DownloadExercises(path, courseId);
        }
        return new DownloadExercises(path, courseId, this.updateCache);
    }

    /**
     * Gives a list of all the courses on the current server, to which the current user has access.
     * Doesn't require login.
     *
     * @return list containing course-objects parsed from JSON
     * @throws TmcCoreException if something went wrong
     */
    public ListenableFuture<List<Course>> listCourses(Credentials credentials, String serverAddress) throws TmcCoreException {
        ClientData.setUserData(credentials.getUsername(), credentials.getPassword());
        ClientData.setServerAddress(serverAddress);
        
        @SuppressWarnings("unchecked")
        ListCourses listCommand = new ListCourses();
        ListenableFuture<List<Course>> listCourses = (ListenableFuture<List<Course>>) threadPool.submit(listCommand);
        return listCourses;
    }

    /**
     * Gives a list of all the exercises relating to a course. Course is found by path. Requires
     * login.
     *
     * @param path to any directory inside a course directory
     * @return list containing exercise-objects parsed from JSON
     * @throws TmcCoreException if there was no course in the given path, or if the path was
     * erroneous
     */
    public ListenableFuture<List<Exercise>> listExercises(String path) throws TmcCoreException {
        checkParameters(path);
        @SuppressWarnings("unchecked")
        ListExercises listCommand = new ListExercises(path);
        ListenableFuture<List<Exercise>> listExercises = (ListenableFuture<List<Exercise>>) threadPool.submit(listCommand);
        return listExercises;
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
    public ListenableFuture<SubmissionResult> submit(String path) throws TmcCoreException {
        checkParameters(path);
        @SuppressWarnings("unchecked")
        Submit submit = new Submit(path);
        ListenableFuture<SubmissionResult> submissionResultListenableFuture = (ListenableFuture<SubmissionResult>) threadPool.submit(submit);
        return submissionResultListenableFuture;
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
    public ListenableFuture<RunResult> test(String path) throws TmcCoreException {
        checkParameters(path);
        @SuppressWarnings("unchecked")
        RunTests testCommand = new RunTests(path);
        ListenableFuture<RunResult> runResultListenableFuture = (ListenableFuture<RunResult>) threadPool.submit(testCommand);
        return runResultListenableFuture;
    }

    /**
     * Fetches unread reviews from the TMC server. Does not mark reviews as read.
     *
     * @param course the course whose reviews are checked
     * @return a list of unread reviews for the given course
     */
    public ListenableFuture<List<Review>> getNewReviews(Course course) throws TmcCoreException {
        ReviewHandler reviewHandler = new ReviewHandler();
        GetUnreadReviews command = new GetUnreadReviews(course, reviewHandler);
        command.checkData();

        @SuppressWarnings("unchecked")
        ListenableFuture<List<Review>> updateFuture
                = (ListenableFuture<List<Review>>) threadPool.submit(command);
        return updateFuture;
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
    public ListenableFuture<List<Exercise>> getNewAndUpdatedExercises(Course course) throws TmcCoreException, IOException {
        ExerciseUpdateHandler updater = new ExerciseUpdateHandler(updateCache);
        GetExerciseUpdates command = new GetExerciseUpdates(course, updater);
        command.checkData();

        @SuppressWarnings("unchecked")
        ListenableFuture<List<Exercise>> updateFuture
                = (ListenableFuture<List<Exercise>>) threadPool.submit(command);
        return updateFuture;
    }

    /**
     * Sends feedback answers to the TMC server.
     *
     * @param answers map of question_id -> answer
     * @param url url that the answers will be sent to
     * @return a HttpResult of the servers reply. It should contain "{status:ok}" if everything goes
     * well
     */
    public ListenableFuture<HttpResult> sendFeedback(Map<String, String> answers, String url) throws TmcCoreException, IOException {
        SendFeedback feedback = new SendFeedback(answers, url);
        feedback.checkData();
        @SuppressWarnings("unchecked")
        ListenableFuture<HttpResult> feedbackListenableFuture
                = (ListenableFuture<HttpResult>) threadPool.submit(feedback);
        return feedbackListenableFuture;
    }

    /**
     * Submits the current exercise to the TMC-server and requests for a paste to be made.
     *
     * @param path inside any exercise directory
     * @return URI object containing location of the paste
     * @throws TmcCoreException if there was no course in the given path, or no exercise in the
     * given path
     */
    public ListenableFuture<URI> paste(String path) throws TmcCoreException {
        checkParameters(path);
        @SuppressWarnings("unchecked")
        Paste paste = new Paste(path);
        ListenableFuture<URI> stringListenableFuture = (ListenableFuture<URI>) threadPool.submit(paste);
        return stringListenableFuture;
    }

    private void checkParameters(String... params) throws TmcCoreException {
        for (String param : params) {
            if (isNullOrEmpty(param)) {
                throw new TmcCoreException("Param empty or null.");
            }
        }
    }
}
