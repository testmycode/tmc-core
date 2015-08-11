package fi.helsinki.cs.tmc.core.communication;

import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.exceptions.ExpiredException;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;
import fi.helsinki.cs.tmc.core.zipping.RootFinder;
import fi.helsinki.cs.tmc.langs.io.zip.Zipper;

import com.google.common.base.Optional;

import net.lingala.zip4j.exception.ZipException;

import java.io.File;
import java.io.IOException;
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
    private final TmcJsonParser tmcJsonParser;
    private final Zipper langsZipper;
    private TmcSettings settings;
    private UrlHelper urlHelper;

    /** Exercise deadline is checked with this date format. */
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSX";

    public ExerciseSubmitter(
            RootFinder rootFinder,
            Zipper zipper,
            UrlCommunicator urlCommunicator,
            TmcJsonParser jsonParser,
            TmcSettings settings) {
        this.urlCommunicator = urlCommunicator;
        this.tmcJsonParser = jsonParser;
        this.rootFinder = rootFinder;
        this.langsZipper = zipper;
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
        if (currentExercise.getDeadline() == null || currentExercise.getDeadline().equals("")) {
            return false;
        }
        Date deadlineDate = new Date();
        Date current = new Date();
        DateFormat format = new SimpleDateFormat(DATE_FORMAT);
        deadlineDate = format.parse(currentExercise.getDeadline());
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
     * @return String with url from which to get results or null if exercise was
     *     not found.
     * @throws IOException if zip creation fails
     */
    public String submit(String currentPath)
            throws IOException, ParseException, ExpiredException, IllegalArgumentException,
                    ZipException, TmcCoreException {
        Exercise currentExercise = initExercise(currentPath);
        return sendZipFile(currentPath, currentExercise, false);
    }

    /**
     * Submits folder of exercise to TMC. Finds it from current directory. Result includes URL of
     * paste.
     *
     * @param currentPath path from which this was called.
     * @return String with url from which to get paste URL or null if exercise
     *     was not found.
     * @throws IOException if zip creation fails
     */
    public String submitPaste(String currentPath)
            throws IOException, ParseException, ExpiredException, IllegalArgumentException,
                    ZipException, TmcCoreException {
        Exercise currentExercise = initExercise(currentPath);
        return sendZipFile(currentPath, currentExercise, true);
    }

    /**
     * Submits folder of exercise to TMC with paste with comment. Finds it from current directory.
     * Result includes URL of paste.
     *
     * @param currentPath path from which this was called.
     * @return String with url from which to get paste URL or null if exercise
     *     was not found.
     * @throws IOException if failed to create zip.
     */
    public String submitPasteWithComment(String currentPath, String comment)
            throws IOException, ParseException, ExpiredException, IllegalArgumentException,
                    ZipException, TmcCoreException {
        Exercise currentExercise = initExercise(currentPath);
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("message_for_paste", comment);
        params.put("paste", "1");
        return sendZipFileWithParams(currentPath, currentExercise, true, params);
    }

    /**
     * Search exercise and throw exception if exercise is expired or not
     * returnable.
     *
     * @throws ParseException to frontend
     * @throws ExpiredException to frontend
     */
    private Exercise initExercise(String currentPath)
            throws ParseException, ExpiredException, IllegalArgumentException, IOException,
                    TmcCoreException {
        Exercise currentExercise = searchExercise(currentPath);
        if (isExpired(currentExercise) || !currentExercise.isReturnable()) {
            throw new ExpiredException("Exercise is expired.");
        }
        return currentExercise;
    }

    private Exercise searchExercise(String currentPath)
            throws IllegalArgumentException, IOException, TmcCoreException {
        Optional<Exercise> currentExercise = findExercise(currentPath);
        if (!currentExercise.isPresent()) {
            throw new IllegalArgumentException("Could not find exercise in this directory");
        }
        return currentExercise.get();
    }

    private String sendSubmissionToServerWithPaste(byte[] file, String url) throws IOException {
        HttpResult result =
                urlCommunicator.makePostWithByteArray(
                        url, file, new HashMap<String, String>(), new HashMap<String, String>());
        return tmcJsonParser.getPasteUrl(result);
    }

    private String sendZipFile(String currentPath, Exercise currentExercise, boolean paste)
            throws IOException {
        String returnUrl = urlHelper.withParams(currentExercise.getReturnUrl());

        byte[] zippedExercise = langsZipper.zip(Paths.get(currentPath));
        String resultUrl;
        if (paste) {
            resultUrl = sendSubmissionToServerWithPaste(zippedExercise, returnUrl);
        } else {
            resultUrl = urlHelper.withParams(sendSubmissionToServer(zippedExercise, returnUrl));
        }
        return resultUrl;
    }

    private String sendZipFileWithParams(
            String currentPath, Exercise currentExercise, boolean paste, Map<String, String> params)
            throws IOException {
        String returnUrl = urlHelper.withParams(currentExercise.getReturnUrl());
        byte[] zippedExercise = langsZipper.zip(Paths.get(currentPath));
        String resultUrl;
        if (paste) {
            resultUrl = sendSubmissionToServerWithPasteAndParams(zippedExercise, returnUrl, params);
        } else {
            resultUrl = sendSubmissionToServerWithParams(zippedExercise, returnUrl, params);
        }
        return resultUrl;
    }

    private String findExerciseFolderToZip(String currentPath) {
        return rootFinder.getRootDirectory(Paths.get(currentPath)).get().toString();
    }

    private String sendSubmissionToServer(byte[] file, String url) throws IOException {
        HttpResult result =
                urlCommunicator.makePostWithByteArray(
                        url, file, new HashMap<String, String>(), new HashMap<String, String>());
        return tmcJsonParser.getSubmissionUrl(result);
    }

    private String sendSubmissionToServerWithParams(
            byte[] file, String url, Map<String, String> params) throws IOException {
        HttpResult result =
                urlCommunicator.makePostWithByteArray(
                        url, file, new HashMap<String, String>(), params);
        return tmcJsonParser.getSubmissionUrl(result);
    }

    private String sendSubmissionToServerWithPasteAndParams(
            byte[] file, String url, Map<String, String> params) throws IOException {
        System.err.println("Paste url: " + url);
        HttpResult result =
                urlCommunicator.makePostWithByteArray(
                        url, file, new HashMap<String, String>(), params);
        return tmcJsonParser.getPasteUrl(result);
    }

    private Optional<Exercise> findExercise(String currentPath)
            throws IllegalArgumentException, IOException, TmcCoreException {
        return findCurrentExercise(findCourseExercises(currentPath), currentPath);
    }

    private List<Exercise> findCourseExercises(String currentPath)
            throws IllegalArgumentException, IOException {
        Optional<Course> currentCourse = this.settings.getCurrentCourse();
        if (!currentCourse.isPresent()) {
            throw new IllegalArgumentException("Not under any course directory");
        }
        return tmcJsonParser.getExercises(currentCourse.get().getId());
    }

    private Optional<Exercise> findCurrentExercise(
            List<Exercise> courseExercises, String currentDir) throws IllegalArgumentException {
        Optional<Path> rootDir = rootFinder.getRootDirectory(Paths.get(currentDir));
        if (!rootDir.isPresent()) {
            throw new IllegalArgumentException("Could not find exercise directory");
        }
        String[] path = rootDir.get().toString().split("\\" + File.separator);
        String directory = path[path.length - 1];
        return getExerciseByName(directory, courseExercises);
    }

    private Optional<Exercise> getExerciseByName(String name, List<Exercise> courseExercises) {
        for (Exercise exercise : courseExercises) {
            if (exercise.getName().contains(name)) {
                return Optional.of(exercise);
            }
        }
        return Optional.absent();
    }

    public String[] getExerciseName(String directoryPath) {
        return directoryPath.split("\\" + File.separator);
    }
}
