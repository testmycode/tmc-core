package fi.helsinki.cs.tmc.core.communication;

import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.exceptions.ExpiredException;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;
import fi.helsinki.cs.tmc.core.zipping.ProjectRootFinder;
import fi.helsinki.cs.tmc.core.zipping.RootFinder;
import fi.helsinki.cs.tmc.langs.util.TaskExecutor;
import fi.helsinki.cs.tmc.langs.util.TaskExecutorImpl;

import com.google.common.base.Optional;
import edu.emory.mathcs.backport.java.util.Collections;
import fi.helsinki.cs.tmc.langs.domain.NoLanguagePluginFoundException;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExerciseSubmitter {

    private RootFinder rootFinder;
    private final UrlCommunicator urlCommunicator;
    private final TmcApi tmcApi;
    private TaskExecutor langs;
    private TmcSettings settings;
    private UrlHelper urlHelper;

    /**
     * Exercise deadline is checked with this date format.
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSX";

    public ExerciseSubmitter(TmcSettings settings) {
        this(
                new ProjectRootFinder(new TaskExecutorImpl(), new TmcApi(settings)),
                new TaskExecutorImpl(),
                new UrlCommunicator(settings),
                new TmcApi(settings),
                settings);
    }

    public ExerciseSubmitter(
            RootFinder rootFinder,
            TaskExecutor taskExecutor,
            UrlCommunicator urlCommunicator,
            TmcApi jsonParser,
            TmcSettings settings) {
        this.urlCommunicator = urlCommunicator;
        this.tmcApi = jsonParser;
        this.rootFinder = rootFinder;
        this.langs = taskExecutor;
        this.settings = settings;
        this.urlHelper = new UrlHelper(settings);
    }

    /**
     * Check if exercise is expired.
     *
     * @param currentExercise Exercise
     * @throws ParseException to frontend
     */
    private boolean isExpired(Exercise currentExercise) throws ParseException {
        if (currentExercise.getDeadline() == null || currentExercise.getDeadline().isEmpty()) {
            return false;
        }
        Date current = new Date();
        DateFormat format = new SimpleDateFormat(DATE_FORMAT);
        Date deadlineDate = format.parse(currentExercise.getDeadline());
        return deadlineGone(current, deadlineDate);
    }

    /**
     * Compare two dates and tell if deadline has gone.
     */
    private boolean deadlineGone(Date current, Date deadline) {
        return current.getTime() > deadline.getTime();
    }

    /**
     * Submits folder of exercise to TMC. Finds it from current directory.
     *
     * @param currentPath path from which this was called.
     * @return URI from which to get results or null if exercise was not found.
     * @throws IOException if zip creation fails
     */
    public URI submit(Path currentPath)
            throws IOException, ParseException, ExpiredException, IllegalArgumentException,
            TmcCoreException, URISyntaxException, NoLanguagePluginFoundException {
        Exercise currentExercise = initExercise(currentPath);
        return sendZipFile(currentPath, currentExercise, false);
    }

    /**
     * Submits folder of exercise to TMC. Finds it from current directory.
     *
     * @param currentPath path from which this was called.
     * @param observer {@link ProgressObserver} that is informed of the submission progress
     * @return URI from which to get results or null if exercise was not found.
     * @throws IOException if zip creation fails
     */
    public URI submit(Path currentPath, ProgressObserver observer)
            throws ParseException, ExpiredException, IllegalArgumentException, IOException,
            TmcCoreException, URISyntaxException, NoLanguagePluginFoundException {
        Exercise currentExercise = initExercise(currentPath);
        return sendZipFile(currentPath, currentExercise, observer, false);
    }

    /**
     * Submits folder of exercise to TMC. Finds it from current directory. Result includes URL of
     * paste.
     *
     * @param currentPath path from which this was called.
     * @return URI from which to get paste URL or null if exercise was not found.
     * @throws IOException if zip creation fails
     */
    public URI submitPaste(Path currentPath)
            throws IOException, ParseException, ExpiredException, IllegalArgumentException,
            TmcCoreException, URISyntaxException, NoLanguagePluginFoundException {
        Exercise currentExercise = initExercise(currentPath);
        return sendZipFile(currentPath, currentExercise, true);
    }

    /**
     * Submits folder of exercise to TMC with paste with comment. Finds it from current directory.
     * Result includes URL of paste.
     *
     * @param currentPath path from which this was called.
     * @return URI from which to get paste URL or null if exercise was not found.
     * @throws IOException if failed to create zip.
     */
    public URI submitPasteWithComment(Path currentPath, String comment)
            throws IOException, ParseException, ExpiredException, IllegalArgumentException,
            TmcCoreException, URISyntaxException, NoLanguagePluginFoundException {
        Exercise currentExercise = initExercise(currentPath);
        HashMap<String, String> params = new HashMap<>();
        params.put("message_for_paste", comment);
        params.put("paste", "1");
        return sendZipFileWithParams(currentPath, currentExercise, true, params);
    }

    /**
     * Requests a code review for a exercise.
     */
    public URI submitWithCodeReviewRequest(Path currentPath, String message)
            throws IOException, ParseException, ExpiredException, IllegalArgumentException,
            TmcCoreException, URISyntaxException, NoLanguagePluginFoundException {
        Exercise currentExercise = initExercise(currentPath);
        HashMap<String, String> params = new HashMap<>();
        params.put("request_review", "1");
        if (!message.isEmpty()) {
            params.put("message_for_reviewer", message);
        }
        return sendZipFileWithParams(currentPath, currentExercise, false, params);
    }

    /**
     * Search exercise and throw exception if exercise is expired or not returnable.
     *
     * @throws ParseException to frontend
     * @throws ExpiredException to frontend
     */
    private Exercise initExercise(Path currentPath)
            throws IllegalArgumentException, IOException, TmcCoreException, URISyntaxException,
            ParseException, ExpiredException {

        Exercise currentExercise = searchExercise(currentPath);
        if (isExpired(currentExercise) || !currentExercise.isReturnable()) {
            throw new ExpiredException("Exercise is expired.");
        }
        return currentExercise;
    }

    private Exercise searchExercise(Path currentPath)
            throws IllegalArgumentException, IOException, TmcCoreException, URISyntaxException {
        Optional<Exercise> currentExercise = findExercise(currentPath);
        if (!currentExercise.isPresent()) {
            throw new IllegalArgumentException("Could not find exercise in this directory");
        }
        return currentExercise.get();
    }

    private URI sendSubmissionToServerWithPaste(byte[] file, URI url) throws IOException {
        HttpResult result
                = urlCommunicator.makePostWithByteArray(
                        url, file, new HashMap<String, String>(), new HashMap<String, String>());
        return tmcApi.getPasteUrl(result);
    }

    private URI sendZipFile(Path currentPath, Exercise currentExercise, boolean paste)
            throws IOException, URISyntaxException, NoLanguagePluginFoundException {
        Optional<ProgressObserver> observer = Optional.absent();
        return sendZipFileWithParams(currentPath, currentExercise, paste, observer, Collections.emptyMap());
    }

    private URI sendZipFile(
            Path currentPath, Exercise currentExercise, ProgressObserver observer, boolean paste)
            throws IOException, URISyntaxException, NoLanguagePluginFoundException {
        return sendZipFileWithParams(currentPath,
                currentExercise,
                paste,
                Optional.of(observer),
                Collections.emptyMap());
    }

    private URI sendZipFileWithParams(
            Path currentPath,
            Exercise currentExercise,
            boolean paste, Map<String, String> params)
            throws IOException, URISyntaxException, NoLanguagePluginFoundException {
        Optional<ProgressObserver> observer = Optional.absent();
        return sendZipFileWithParams(currentPath, currentExercise, paste, observer, params);
    }

    private URI sendZipFileWithParams(
            Path currentPath, Exercise currentExercise, boolean paste,
            Optional<ProgressObserver> observer, Map<String, String> params)
            throws IOException, URISyntaxException, NoLanguagePluginFoundException {
        URI returnUrl = urlHelper.withParams(currentExercise.getReturnUrl());
        if (observer.isPresent()) {
            observer.get().progress("zipping exercise");
        }
        byte[] zippedExercise = langs.compressProject(currentPath);
        if (observer.isPresent()) {
            observer.get().progress("submitting exercise");
        }
        URI resultUrl;
        if (paste) {
            resultUrl = sendSubmissionToServerWithPasteAndParams(zippedExercise, returnUrl, params);
        } else {
            resultUrl = sendSubmissionToServerWithParams(zippedExercise, returnUrl, params);
        }
        return resultUrl;
    }

    private Path findExerciseFolderToZip(Path currentPath) {
        return rootFinder.getRootDirectory(currentPath).get();
    }

    private URI sendSubmissionToServer(byte[] file, URI url) throws IOException {
        HttpResult result
                = urlCommunicator.makePostWithByteArray(
                        url, file, new HashMap<String, String>(), new HashMap<String, String>());
        return tmcApi.getSubmissionUrl(result);
    }

    private URI sendSubmissionToServerWithParams(
            byte[] file, URI url, Map<String, String> params) throws IOException {
        HttpResult result
                = urlCommunicator.makePostWithByteArray(
                        url, file, new HashMap<String, String>(), params);
        return tmcApi.getSubmissionUrl(result);
    }

    private URI sendSubmissionToServerWithPasteAndParams(
            byte[] file, URI url, Map<String, String> params) throws IOException {
        HttpResult result
                = urlCommunicator.makePostWithByteArray(
                        url, file, new HashMap<String, String>(), params);
        return tmcApi.getPasteUrl(result);
    }

    private Optional<Exercise> findExercise(Path currentPath)
            throws IllegalArgumentException, IOException, TmcCoreException, URISyntaxException {
        return findCurrentExercise(findCourseExercises(currentPath), currentPath);
    }

    private List<Exercise> findCourseExercises(Path currentPath)
            throws IllegalArgumentException, IOException, URISyntaxException {
        Optional<Course> currentCourse = this.settings.getCurrentCourse();
        if (!currentCourse.isPresent()) {
            throw new IllegalArgumentException("Not under any course directory");
        }
        return tmcApi.getExercises(currentCourse.get().getId());
    }

    private Optional<Exercise> findCurrentExercise(
            List<Exercise> courseExercises, Path currentDir) throws IllegalArgumentException {
        Optional<Path> rootDir = rootFinder.getRootDirectory(currentDir);
        if (!rootDir.isPresent()) {
            throw new IllegalArgumentException("Could not find exercise directory");
        }
        String name = rootDir.get().getFileName().toString();
        return getExerciseByName(name, courseExercises);
    }

    private Optional<Exercise> getExerciseByName(String name, List<Exercise> courseExercises) {
        for (Exercise exercise : courseExercises) {
            if (exercise.getName().contains(name)) {
                return Optional.of(exercise);
            }
        }
        return Optional.absent();
    }
}
