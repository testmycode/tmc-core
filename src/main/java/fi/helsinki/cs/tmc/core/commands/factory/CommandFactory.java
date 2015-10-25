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

public class CommandFactory {

    public static Command<Boolean> getVerifyCredentialsCmd(
            TmcSettings settings, UrlCommunicator communicator) {
        return new VerifyCredentials(settings, communicator);
    }

    public static Command<Boolean> getVerifyCredentialsCmd(TmcSettings settings) {
        return new VerifyCredentials(settings, new UrlCommunicator(settings));
    }


    public static Command<Course> getCourseCmd(TmcSettings settings, URI courseUri) {
        return new GetCourse(settings,courseUri);
    }

    public static Command<Course> getCourseCmd(
            TmcSettings settings, String courseName) throws TmcCoreException {
        return new GetCourse(settings, courseName);
    }

    public static Command<List<Exercise>> getDownloadExercisesCmd(TmcSettings settings,
                                                                   Path path,
                                                                   int courseId,
                                                                   ProgressObserver observer,
                                                                   ExerciseChecksumCache cache) {
        return new DownloadExercises(settings, path, courseId, observer, cache);
    }

    public static Command<Boolean> getDownloadModelSolutionCmd(
            TmcSettings settings, Exercise exercise) {
        return new DownloadModelSolution(settings, exercise);
    }

    public static Command<List<Course>> getListCoursesCmd(TmcSettings settings) {
        return new ListCourses(settings);
    }

    public static Command<SubmissionResult> getSubmitCmd( TmcSettings settings,
                                                          ExerciseSubmitter submitter,
                                                          SubmissionPoller submissionPoller,
                                                          Path path,
                                                          ProgressObserver observer) {
        return new Submit(settings, submitter, submissionPoller, path, observer);
    }

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

    public static Command<RunResult> getRunTestsCmd(TmcSettings settings, Path path) {
        return new RunTests(settings, path);
    }

    public static Command<ValidationResult> getRunCheckStyleCmd(Path path) {
        return new RunCheckStyle(path);
    }

    public static Command<List<Review>> getUnreadReviewsCmd(
            ReviewHandler reviewHandler, Course course) {
        return new GetUnreadReviews(course, reviewHandler);
    }

    public static Command<List<Review>> getUnreadReviewsCmd(TmcSettings settings, Course course) {
        ReviewHandler reviewHandler = new ReviewHandler(new TmcApi(settings));
        return new GetUnreadReviews(course, reviewHandler);
    }

    public static Command<List<Exercise>> getExerciseUpdatesCmd(
            Course course, ExerciseUpdateHandler handler) {
        return new GetExerciseUpdates(course, handler);
    }

    public static Command<List<Exercise>> getExerciseUpdatesCmd(
            TmcSettings settings,
            ExerciseChecksumCache updateCache, Course course) throws TmcCoreException {

        ExerciseUpdateHandler updater =
                new ExerciseUpdateHandler(updateCache, new TmcApi(settings));
        return new GetExerciseUpdates(course, updater);
    }

    public static Command<HttpResult> getSendFeedbackCmd(
            TmcSettings settings, Map<String, String> answers, URI url) {
        return new SendFeedback(settings, answers, url);
    }

    public static Command<URI> getPasteWithCommentCmd(
            TmcSettings settings, Path path, String comment) {
        return new PasteWithComment(settings, path, comment);
    }

    public static Command<URI> getRequestCodeReviewCmd(
            TmcSettings settings, Path path, String comment) {
        return new RequestCodeReview(settings, path, comment);
    }

    public static Command<List<HttpResult>> getSendSpywareDiffsCmd(
            TmcSettings settings, byte[] spywareDiffs) {
        return new SendSpywareDiffs(settings, new DiffSender(settings), spywareDiffs);
    }

}
