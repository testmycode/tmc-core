package hy.tmc.cli.backend;

import static com.google.common.base.Strings.isNullOrEmpty;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import fi.helsinki.cs.tmc.langs.RunResult;
import hy.tmc.cli.backend.communication.HttpResult;
import hy.tmc.cli.domain.Course;
import hy.tmc.cli.domain.Exercise;
import hy.tmc.cli.domain.submission.SubmissionResult;
import hy.tmc.cli.frontend.communication.commands.Authenticate;
import hy.tmc.cli.frontend.communication.commands.ChooseServer;
import hy.tmc.cli.frontend.communication.commands.Command;
import hy.tmc.cli.frontend.communication.commands.DownloadExercises;
import hy.tmc.cli.frontend.communication.commands.Help;
import hy.tmc.cli.frontend.communication.commands.ListCourses;
import hy.tmc.cli.frontend.communication.commands.ListExercises;
import hy.tmc.cli.frontend.communication.commands.Logout;
import hy.tmc.cli.frontend.communication.commands.Paste;
import hy.tmc.cli.frontend.communication.commands.RunTests;
import hy.tmc.cli.frontend.communication.commands.SendFeedback;
import hy.tmc.cli.frontend.communication.commands.Submit;
import hy.tmc.core.exceptions.ProtocolException;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

public class TmcCore {

    private ListeningExecutorService threadPool;

    /**
     * The TmcCore that can be used as a standalone businesslogic for any tmc
     * client application. The TmcCore provides all the essential backend
     * functionalities as public methods.
     */
    public TmcCore() {
        threadPool = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
    }

    /**
     * For dependency injection of threadpool.
     *
     * @param pool thread threadpool which to use with the core
     */
    public TmcCore(ListeningExecutorService pool) {
        this.threadPool = pool;
    }

    public ListeningExecutorService getThreadPool() {
        return threadPool;
    }

    /**
     * Authenticates the given user on the server, and saves the data into
     * memory.
     *
     * @param username to authenticate with
     * @param password to authenticate with
     * @return A future-object containing true or false on success or fail
     * @throws ProtocolException if something in the given input was wrong
     */
    public ListenableFuture<Boolean> login(String username, String password) throws ProtocolException {
        checkParameters(username, password);
        Authenticate login = new Authenticate(username, password);
        @SuppressWarnings("unchecked")
        ListenableFuture<Boolean> stringListenableFuture = (ListenableFuture<Boolean>) threadPool.submit(login);
        return stringListenableFuture;
    }

    /**
     * Logs the user out, in other words clears the saved userdata from memory.
     * Always clears the user data.
     *
     * @return A future-object containing true if user was logged in previously,
     * and false if nobody was logged in
     * @throws ProtocolException if something in the given input was wrong
     */
    public ListenableFuture<Boolean> logout() throws ProtocolException {
        @SuppressWarnings("unchecked")
        Logout logoutCommand = new Logout();
        ListenableFuture<Boolean> logout = (ListenableFuture<Boolean>) threadPool.submit(logoutCommand);
        return logout;
    }

    /**
     * Selects the given server as the working TMC-server. All requests,
     * submits, etc. will be made to that server.
     *
     * @param serverAddress this will be the new TMC-server address
     * @return A future-object containing true or false on success or fail
     * @throws ProtocolException if something in the given input was wrong
     */
    public ListenableFuture<Boolean> selectServer(String serverAddress) throws ProtocolException {
        checkParameters(serverAddress);
        @SuppressWarnings("unchecked")
        ChooseServer chooseCommand = new ChooseServer(serverAddress);
        ListenableFuture<Boolean> stringListenableFuture = (ListenableFuture<Boolean>) threadPool.submit(chooseCommand);
        return stringListenableFuture;
    }

    /**
     * Downloads the exercise files of a given source to the given directory. If
     * files exist, overrides everything except the source folder and files
     * specified in .tmcproject.yml Requires login.
     *
     * @param path where it downloads the exercises
     * @param courseId ID of course to download
     * @return A future-object containing true or false on success or fail
     * @throws ProtocolException if something in the given input was wrong
     */
    public ListenableFuture<String> downloadExercises(String path, String courseId) throws ProtocolException {
        checkParameters(path, courseId);
        @SuppressWarnings("unchecked")
        DownloadExercises downloadCommand = new DownloadExercises(path, courseId);
        ListenableFuture<String> stringListenableFuture = (ListenableFuture<String>) threadPool.submit(downloadCommand);
        return stringListenableFuture;
    }

    /**
     * Displays a help message containing the names of valid commands on the
     * server side.
     *
     * @return future-object containing a string with the help information.
     * @throws ProtocolException if something went wrong.
     */
    public ListenableFuture<String> help() throws ProtocolException {
        @SuppressWarnings("unchecked")
        Help helpCommand = new Help();
        ListenableFuture<String> help = (ListenableFuture<String>) threadPool.submit(helpCommand);
        return help;
    }

    /**
     * Gives a list of all the courses on the current server, to which the
     * current user has access. Doesn't require login.
     *
     * @return list containing course-objects parsed from JSON
     * @throws ProtocolException if something went wrong
     */
    public ListenableFuture<List<Course>> listCourses() throws ProtocolException {
        @SuppressWarnings("unchecked")
        ListCourses listCommand = new ListCourses();
        ListenableFuture<List<Course>> listCourses = (ListenableFuture<List<Course>>) threadPool.submit(listCommand);
        return listCourses;
    }

    /**
     * Gives a list of all the exercises relating to a course. Course is found
     * by path. Requires login.
     *
     * @param path to any directory inside a course directory
     * @return list containing exercise-objects parsed from JSON
     * @throws ProtocolException if there was no course in the given path, or if
     * the path was erroneous
     */
    public ListenableFuture<List<Exercise>> listExercises(String path) throws ProtocolException {
        checkParameters(path);
        @SuppressWarnings("unchecked")
        ListExercises listCommand = new ListExercises(path);
        ListenableFuture<List<Exercise>> listExercises = (ListenableFuture<List<Exercise>>) threadPool.submit(listCommand);
        return listExercises;
    }

    /**
     * Submits an exercise in the given path to the TMC-server. Looks for a
     * build.xml or equivalent file upwards in the path to determine exercise
     * folder. Requires login.
     *
     * @param path inside any exercise directory
     * @return SubmissionResult object containing details of the tests run on
     * server
     * @throws ProtocolException if there was no course in the given path, no
     * exercise in the given path, or not logged in
     */
    public ListenableFuture<SubmissionResult> submit(String path) throws ProtocolException {
        checkParameters(path);
        @SuppressWarnings("unchecked")
        Submit submit = new Submit(path);
        ListenableFuture<SubmissionResult> submissionResultListenableFuture = (ListenableFuture<SubmissionResult>) threadPool.submit(submit);
        return submissionResultListenableFuture;
    }

    /**
     * Runs tests on the specified directory. Looks for a build.xml or
     * equivalent file upwards in the path to determine exercise folder. Doesn't
     * require login.
     *
     * @param path inside any exercise directory
     * @return RunResult object containing details of the tests run
     * @throws ProtocolException if there was no course in the given path, or no
     * exercise in the given path
     */
    public ListenableFuture<RunResult> test(String path) throws ProtocolException {
        checkParameters(path);
        @SuppressWarnings("unchecked")
        RunTests testCommand = new RunTests(path);
        ListenableFuture<RunResult> runResultListenableFuture = (ListenableFuture<RunResult>) threadPool.submit(testCommand);
        return runResultListenableFuture;
    }

    public ListenableFuture<HttpResult> sendFeedback(Map<String, String> answers, String url) throws ProtocolException, IOException {
        SendFeedback feedback = new SendFeedback(answers, url);
        feedback.checkData();
        @SuppressWarnings("unchecked")
        ListenableFuture<HttpResult> feedbackListenableFuture = 
                (ListenableFuture<HttpResult>) threadPool.submit(feedback);
        return feedbackListenableFuture;
    }

    /**
     * Submits the current exercise to the TMC-server and requests for a paste
     * to be made.
     *
     * @param path inside any exercise directory
     * @return URI object containing location of the paste
     * @throws ProtocolException if there was no course in the given path, or no
     * exercise in the given path
     */
    public ListenableFuture<URI> paste(String path) throws ProtocolException {
        checkParameters(path);
        @SuppressWarnings("unchecked")
        Paste paste = new Paste(path);
        ListenableFuture<URI> stringListenableFuture = (ListenableFuture<URI>) threadPool.submit(paste);
        return stringListenableFuture;
    }


    public ListenableFuture<?> submitTask(Callable<?> callable) {
        return threadPool.submit(callable);
    }

    private void checkParameters(String... params) throws ProtocolException {
        for (String param : params) {
            if (isNullOrEmpty(param)) {
                throw new ProtocolException("Param empty or null.");
            }
        }
    }
}
