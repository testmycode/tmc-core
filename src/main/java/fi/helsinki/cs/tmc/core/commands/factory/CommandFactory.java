package fi.helsinki.cs.tmc.core.commands.factory;

import fi.helsinki.cs.tmc.core.cache.ExerciseChecksumCache;
import fi.helsinki.cs.tmc.core.commands.*;
import fi.helsinki.cs.tmc.core.communication.*;
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
import fi.helsinki.cs.tmc.langs.util.TaskExecutorImpl;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Handles the creation of different Commands. Mainly used by
 * {@link fi.helsinki.cs.tmc.core.TmcCore}
 */

public class CommandFactory {

    /**
     * .
     * @see VerifyCredentials#VerifyCredentials(TmcSettings, UrlCommunicator)
     */
    public static Command<Boolean> getVerifyCredentialsCmd(
            TmcSettings settings, UrlCommunicator communicator) {
        return new VerifyCredentials(settings, communicator);
    }

    /**
     * .
     * @see VerifyCredentials#VerifyCredentials(TmcSettings, UrlCommunicator)
     */
    public static Command<Boolean> getVerifyCredentialsCmd(TmcSettings settings) {
        return new VerifyCredentials(settings, new UrlCommunicator(settings));
    }

    /**
     * .
     * @see GetCourse#GetCourse(TmcSettings, String)
     */
    public static Command<Course> getCourseCmd(TmcSettings settings, URI courseUri) {
        return new GetCourse(settings,courseUri);
    }

    /**
     * .
     * @see GetCourse#GetCourse(TmcSettings, String)
     */
    public static Command<Course> getCourseCmd(
            TmcSettings settings, String courseName) throws TmcCoreException {
        return new GetCourse(settings, courseName);
    }

    /**
     * .
     * @see DownloadExercises#DownloadExercises(TmcSettings, Path, int, ProgressObserver, ExerciseChecksumCache)
     */
    public static Command<List<Exercise>> getDownloadExercisesCmd(TmcSettings settings,
                                                                   Path path,
                                                                   int courseId,
                                                                   ProgressObserver observer,
                                                                   ExerciseChecksumCache cache) {

        return new DownloadExercises(settings, path, courseId, observer, cache);
    }

    /**
     * .
     * @see DownloadModelSolution#DownloadModelSolution(TmcSettings, Exercise)
     */
    public static Command<Boolean> getDownloadModelSolutionCmd(
            TmcSettings settings, Exercise exercise) {
        return new DownloadModelSolution(settings, exercise);
    }

    /**
     * .
     * @see ListCourses#ListCourses(TmcSettings)
     */
    public static Command<List<Course>> getListCoursesCmd(TmcSettings settings) {
        return new ListCourses(settings);
    }

    /**
     * .
     * @see Submit#Submit(TmcSettings, ExerciseSubmitter, SubmissionPoller, Path, ProgressObserver)
     */
    public static Command<SubmissionResult> getSubmitCmd( TmcSettings settings,
                                                          ExerciseSubmitter submitter,
                                                          SubmissionPoller submissionPoller,
                                                          Path path,
                                                          ProgressObserver observer) {
        return new Submit(settings, submitter, submissionPoller, path, observer);
    }

    /**
     * .
     * @see Submit#Submit(TmcSettings, ExerciseSubmitter, SubmissionPoller, Path, ProgressObserver)
     */
    public static Command<SubmissionResult> getSubmitCmd(
            TmcSettings settings, Path path, ProgressObserver observer) {

        UrlCommunicator communicator = new UrlCommunicator(settings);
        TmcApi tmcApi = new TmcApi(communicator, settings);

        ExerciseSubmitter exerciseSubmitter =
                new ExerciseSubmitter(
                        new ProjectRootFinder(tmcApi),
                        new TaskExecutorImpl(),
                        communicator,
                        tmcApi,
                        settings);

        SubmissionPoller submissionPoller = new SubmissionPoller(tmcApi);
        return getSubmitCmd(settings, exerciseSubmitter, submissionPoller, path, observer);
    }

    /**
     * .
     * @see RunTests#RunTests(TmcSettings, Path)
     */
    public static Command<RunResult> getRunTestsCmd(TmcSettings settings, Path path) {
        return new RunTests(settings, path);
    }

    /**
     * .
     * @see RunCheckStyle#RunCheckStyle(Path)
     */
    public static Command<ValidationResult> getRunCheckStyleCmd(Path path) {
        return new RunCheckStyle(path);
    }

    /**
     * .
     * @see GetUnreadReviews#GetUnreadReviews(Course, ReviewHandler)
     */
    public static Command<List<Review>> getUnreadReviewsCmd(
            ReviewHandler reviewHandler, Course course) {
        return new GetUnreadReviews(course, reviewHandler);
    }

    /**
     * .
     * @see GetUnreadReviews#GetUnreadReviews(Course, ReviewHandler)
     */
    public static Command<List<Review>> getUnreadReviewsCmd(TmcSettings settings, Course course) {
        ReviewHandler reviewHandler = new ReviewHandler(new TmcApi(settings));
        return new GetUnreadReviews(course, reviewHandler);
    }

    /**
     * .
     * @see GetExerciseUpdates#GetExerciseUpdates(Course, ExerciseUpdateHandler)
     */
    public static Command<List<Exercise>> getExerciseUpdatesCmd(
            Course course, ExerciseUpdateHandler handler) {
        return new GetExerciseUpdates(course, handler);
    }

    /**
     * .
     * @see GetExerciseUpdates#GetExerciseUpdates(Course, ExerciseUpdateHandler)
     */
    public static Command<List<Exercise>> getExerciseUpdatesCmd(
            TmcSettings settings,
            ExerciseChecksumCache updateCache, Course course) throws TmcCoreException {

        ExerciseUpdateHandler updater =
                new ExerciseUpdateHandler(updateCache, new TmcApi(settings));
        return new GetExerciseUpdates(course, updater);
    }

    /**
     * .
     * @see SendFeedback#SendFeedback(TmcSettings, Map, URI)
     */
    public static Command<HttpResult> getSendFeedbackCmd(
            TmcSettings settings, Map<String, String> answers, URI url) {
        return new SendFeedback(settings, answers, url);
    }

    /**
     * .
     * @see PasteWithComment#PasteWithComment(TmcSettings, Path, String)
     */
    public static Command<URI> getPasteWithCommentCmd(
            TmcSettings settings, Path path, String comment) {
        return new PasteWithComment(settings, path, comment);
    }

    /**
     * .
     * @see RequestCodeReview#RequestCodeReview(TmcSettings, Path, String)
     */
    public static Command<URI> getRequestCodeReviewCmd(
            TmcSettings settings, Path path, String comment) {
        return new RequestCodeReview(settings, path, comment);
    }

    /**
     * .
     * @see SendSpywareDiffs#SendSpywareDiffs(TmcSettings, DiffSender, byte[])
     */
    public static Command<List<HttpResult>> getSendSpywareDiffsCmd(
            TmcSettings settings, byte[] spywareDiffs) {
        return new SendSpywareDiffs(settings, new DiffSender(settings), spywareDiffs);
    }

}
