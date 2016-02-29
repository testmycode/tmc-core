package fi.helsinki.cs.tmc.core;

import fi.helsinki.cs.tmc.core.commands.DownloadCompletedExercises;
import fi.helsinki.cs.tmc.core.commands.DownloadOrUpdateExercises;
import fi.helsinki.cs.tmc.core.commands.DownloadModelSolution;
import fi.helsinki.cs.tmc.core.commands.GetCourseDetails;
import fi.helsinki.cs.tmc.core.commands.GetUpdatableExercises;
import fi.helsinki.cs.tmc.core.commands.GetUnreadReviews;
import fi.helsinki.cs.tmc.core.commands.ListCourses;
import fi.helsinki.cs.tmc.core.commands.PasteWithComment;
import fi.helsinki.cs.tmc.core.commands.RequestCodeReview;
import fi.helsinki.cs.tmc.core.commands.RunCheckStyle;
import fi.helsinki.cs.tmc.core.commands.RunTests;
import fi.helsinki.cs.tmc.core.commands.SendFeedback;
import fi.helsinki.cs.tmc.core.commands.SendSpywareEvents;
import fi.helsinki.cs.tmc.core.commands.Submit;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.domain.submission.FeedbackAnswer;
import fi.helsinki.cs.tmc.core.domain.submission.SubmissionResult;
import fi.helsinki.cs.tmc.core.holders.TmcLangsHolder;
import fi.helsinki.cs.tmc.core.holders.TmcSettingsHolder;
import fi.helsinki.cs.tmc.core.spyware.LoggableEvent;
import fi.helsinki.cs.tmc.langs.abstraction.ValidationResult;
import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.util.TaskExecutor;

import java.net.URI;
import java.util.List;
import java.util.concurrent.Callable;

public class TmcCore {

    public TmcCore(TmcSettings settings, TaskExecutor tmcLangs) {
        TmcSettingsHolder.set(settings);
        TmcLangsHolder.set(tmcLangs);
    }

    public Callable<List<Exercise>> downloadOrUpdateExercises(
            ProgressObserver observer,
            List<Exercise> exercises) {
        return new DownloadOrUpdateExercises(observer, exercises);
    }


    public Callable<Course> getCourseDetails(
            ProgressObserver observer,
            Course course) {
        return new GetCourseDetails(observer, course);
    }

    public Callable<List<Course>> listCourses(ProgressObserver observer) {
        return new ListCourses(observer);
    }

    public Callable<URI> pasteWithComment(
            ProgressObserver observer,
            Exercise exercise,
            String message) {
        return new PasteWithComment(observer, exercise, message);
    }

    public Callable<ValidationResult> runCheckStyle(
            ProgressObserver observer,
            Exercise exercise) {
        return new RunCheckStyle(observer, exercise);
    }

    public Callable<RunResult> runTests(
            ProgressObserver observer,
            Exercise exercise) {
        return new RunTests(observer, exercise);
    }

    public Callable<Boolean> sendFeedback(
            ProgressObserver observer,
            List<FeedbackAnswer> answers,
            URI feedbackUri) {
        return new SendFeedback(observer, answers, feedbackUri);
    }

    public Callable<Void> sendSpywareEvents(
            ProgressObserver observer,
            Course currentCourse,
            List<LoggableEvent> events) {
        return new SendSpywareEvents(observer, currentCourse, events);
    }

    public Callable<SubmissionResult> submit(
            ProgressObserver observer,
            Exercise exercise) {
        return new Submit(observer, exercise);
    }

    public Callable<List<Exercise>> getExerciseUpdates(ProgressObserver observer) {
        return new GetUpdatableExercises(observer);
    }

    /**
     * NOT IMPLEMENTED!
     *
     * <p>TARGET: CORE MILESTONE 2.
     */
    public Callable<Void> getUnreadReviews(ProgressObserver observer) {
        return new GetUnreadReviews(observer);
    }

    /**
     * NOT IMPLEMENTED!
     *
     * <p>TARGET: CORE MILESTONE 2.
     */
    public Callable<Void> requestCodeReview(ProgressObserver observer) {
        return new RequestCodeReview(observer);
    }

    /**
     * NOT IMPLEMENTED!
     *
     * <p>TARGET: CORE MILESTONE 2.
     */
    public Callable<Void> downloadCompletedExercises(ProgressObserver observer) {
        return new DownloadCompletedExercises(observer);
    }

    /**
     * NOT IMPLEMENTED!
     *
     * <p>TARGET: CORE MILESTONE 3.
     */
    public Callable<Void> downloadModelSolution(ProgressObserver observer) {
        return new DownloadModelSolution(observer);
    }


}
