package hy.tmc.core.communication;

import com.google.common.base.Optional;

import hy.tmc.core.domain.Course;
import hy.tmc.core.domain.Exercise;
import hy.tmc.core.exceptions.ExpiredException;
import hy.tmc.core.exceptions.TmcCoreException;
import hy.tmc.core.zipping.RootFinder;
import hy.tmc.core.zipping.ZipMaker;
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

import net.lingala.zip4j.exception.ZipException;
import org.apache.http.entity.mime.content.FileBody;

public class CourseSubmitter {

    private RootFinder rootFinder;
    private ZipMaker zipper;
    private final UrlCommunicator urlCommunicator;
    private final TmcJsonParser tmcJsonParser;

    /**
     * Exercise deadline is checked with this date format
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSX";

    private String submissionZipPath;

    public CourseSubmitter(RootFinder rootFinder, ZipMaker zipper, 
            UrlCommunicator urlCommunicator, TmcJsonParser jsonParser) {
        this.urlCommunicator = urlCommunicator;
        this.tmcJsonParser = jsonParser;
        this.zipper = zipper;
        this.rootFinder = rootFinder;
    }

    /**
     * Check if exercise is expired.
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
     * @return String with url from which to get results or null if exercise was not found.
     * @throws IOException if failed to create zip.
     * @throws java.text.ParseException
     * @throws hy.tmc.core.exceptions.ExpiredException
     */
    public String submit(String currentPath) throws IOException, ParseException, ExpiredException, IllegalArgumentException, ZipException, TmcCoreException {
        Exercise currentExercise = initExercise(currentPath);
        return sendZipFile(currentPath, currentExercise, false);
    }

    /**
     * Submits folder of exercise to TMC. Finds it from current directory. Result includes URL of
     * paste.
     * @param currentPath path from which this was called.
     * @return String with url from which to get paste URL or null if exercise was not found.
     * @throws IOException if failed to create zip.
     * @throws java.text.ParseException
     * @throws hy.tmc.core.exceptions.ExpiredException
     */
    public String submitPaste(String currentPath) throws IOException, ParseException, ExpiredException, IllegalArgumentException, ZipException, TmcCoreException {
        Exercise currentExercise = initExercise(currentPath);
        System.err.println("Exercise: " + currentExercise.getCourseName());
        return sendZipFile(currentPath, currentExercise, true);
    }
    
    /**
     * Submits folder of exercise to TMC with paste with comment. Finds it from current directory. Result includes URL of
     * paste.
     * @param currentPath path from which this was called.
     * @return String with url from which to get paste URL or null if exercise was not found.
     * @throws IOException if failed to create zip.
     * @throws java.text.ParseException
     * @throws hy.tmc.core.exceptions.ExpiredException
     */
    public String submitPasteWithComment(String currentPath, String comment) throws IOException, ParseException, ExpiredException, IllegalArgumentException, ZipException, TmcCoreException {
        Exercise currentExercise = initExercise(currentPath);
        System.err.println("Exercise: " + currentExercise.getCourseName());
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("message_for_paste", comment);
        params.put("paste", "1");
        return sendZipFileWithParams(currentPath, currentExercise, true, params);
    }
    
    /**
     * Search exercise and throw exception if exercise is expired or not returnable.
     * @throws ParseException to frontend
     * @throws ExpiredException to frontend
     */
    private Exercise initExercise(String currentPath) throws ParseException, ExpiredException, IllegalArgumentException, IOException, TmcCoreException {
        Exercise currentExercise = searchExercise(currentPath);
        if(isExpired(currentExercise) || !currentExercise.isReturnable()){
            deleteZipIfExists();
            throw new ExpiredException("Exercise is expired.");
        }
        return currentExercise;
    }

    private Exercise searchExercise(String currentPath) throws IllegalArgumentException, IOException, TmcCoreException {
        Optional<Exercise> currentExercise = findExercise(currentPath);
        if (!currentExercise.isPresent()) {
            deleteZipIfExists();
            throw new IllegalArgumentException("Could not find exercise in this directory");
        }
        return currentExercise.get();
    }

    private String sendSubmissionToServerWithPaste(
            String submissionZipPath,
            String url) throws IOException {
        final String pasteExtensionForTmcServer = "&paste=1";
        HttpResult result = urlCommunicator.makePostWithFile(
                new FileBody(new File(submissionZipPath)),
                url + pasteExtensionForTmcServer,
                new HashMap<String, String>()
        );
        return tmcJsonParser.getPasteUrl(result);
    }

    private String sendZipFile(String currentPath, Exercise currentExercise, boolean paste) throws IOException, ZipException {
        String submissionExtension = File.separator + "submission.zip";

        this.submissionZipPath = currentPath + submissionExtension;
        String returnUrl = currentExercise.getReturnUrlWithApiVersion();
        deleteZipIfExists();
        zip(findExerciseFolderToZip(currentPath), submissionZipPath);
        String resultUrl;
        if (paste) {
            resultUrl = sendSubmissionToServerWithPaste(submissionZipPath, returnUrl);
        } else {
            resultUrl = sendSubmissionToServer(submissionZipPath, returnUrl);
        }
        deleteZipIfExists();
        return resultUrl;
    }
    
    private String sendZipFileWithParams(String currentPath, Exercise currentExercise, boolean paste, Map<String, String> params) throws IOException, ZipException {
        String submissionExtension = File.separator + "submission.zip";

        this.submissionZipPath = currentPath + submissionExtension;
        String returnUrl = currentExercise.getReturnUrlWithApiVersion();
        System.out.println("Returnurl: " + returnUrl);
        deleteZipIfExists();
        zip(findExerciseFolderToZip(currentPath), submissionZipPath);
        String resultUrl;
        if (paste) {
            resultUrl = sendSubmissionToServerWithPasteAndParams(submissionZipPath, returnUrl, params);
        } else {
            resultUrl = sendSubmissionToServerWithParams(submissionZipPath, returnUrl, params);
        }
        deleteZipIfExists();
        return resultUrl;
    }

    private String findExerciseFolderToZip(String currentPath) {
        return rootFinder.getRootDirectory(
                Paths.get(currentPath)
        ).get().toString();
    }

     private String sendSubmissionToServer(String submissionZipPath, String url) throws IOException {
        HttpResult result = urlCommunicator.makePostWithFile(
                new FileBody(new File(submissionZipPath)), 
                url, 
                new HashMap<String, String>()
        );
        return tmcJsonParser.getSubmissionUrl(result);
    }
     
     private String sendSubmissionToServerWithParams(String submissionZipPath, String url, Map<String, String> params) throws IOException{
         HttpResult result = urlCommunicator.makePostWithFileAndParams(
                new FileBody(new File(submissionZipPath)), 
                url, 
                new HashMap<String, String>(), 
                params
        );
        return tmcJsonParser.getSubmissionUrl(result);
     }
     
     private String sendSubmissionToServerWithPasteAndParams(
            String submissionZipPath,
            String url, Map<String, String> params) throws IOException {
        final String pasteExtensionForTmcServer = "&paste=1";
        System.err.println("Url " + (url));
        HttpResult result = urlCommunicator.makePostWithFileAndParams(
                new FileBody(new File(submissionZipPath)),
                url,
                new HashMap<String, String>(), 
                params
        );
        return tmcJsonParser.getPasteUrl(result);
    }

    private Optional<Exercise> findExercise(String currentPath) throws IllegalArgumentException, IOException, TmcCoreException {
        return findCurrentExercise(findCourseExercises(currentPath), currentPath);
    }

    private List<Exercise> findCourseExercises(String currentPath) throws IllegalArgumentException, IOException, TmcCoreException {
        Optional<Course> currentCourse = rootFinder.getCurrentCourse(currentPath);
        if (!currentCourse.isPresent()) {
            deleteZipIfExists();
            throw new IllegalArgumentException("Not under any course directory");
        }
        List<Exercise> courseExercises = tmcJsonParser.getExercises(currentCourse.get().getId());
        return courseExercises;
    }

    private void zip(String exerciseFolderToZip, String currentPath) throws ZipException {
        try {
            this.zipper.zip(exerciseFolderToZip, currentPath);
        }
        catch (ZipException ex) {
            throw new ZipException("Zipping failed because of " + ex.getMessage());
        }
    }

    private Optional<Exercise> findCurrentExercise(List<Exercise> courseExercises, String currentDir) throws IllegalArgumentException {
        Optional<Path> rootDir = rootFinder.getRootDirectory(Paths.get(currentDir));
        if (!rootDir.isPresent()) {
            deleteZipIfExists();
            throw new IllegalArgumentException("Could not find exercise directory");
        }
        String[] path = rootDir.get().toString().split(File.separator);
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
        return directoryPath.split(File.separator);
    }

    /**
     * If class submissionZipPath is defined in sendZipFile-method 
     * and the file in defined path exists, it will be removed.
     * This method should be invoked always when submit-function fails.
     */
    private void deleteZipIfExists() {
        if (submissionZipPath != null) {
            File zip = new File(submissionZipPath);
            if (zip.exists()) {
                zip.delete();
            }
        }
    }
}
