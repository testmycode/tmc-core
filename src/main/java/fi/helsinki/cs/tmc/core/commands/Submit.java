package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.ExerciseSubmitter;
import fi.helsinki.cs.tmc.core.communication.SubmissionPoller;
import fi.helsinki.cs.tmc.core.communication.TmcJsonParser;
import fi.helsinki.cs.tmc.core.communication.UrlCommunicator;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.submission.SubmissionResult;
import fi.helsinki.cs.tmc.core.exceptions.ExpiredException;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;
import fi.helsinki.cs.tmc.core.zipping.ProjectRootFinder;

import com.google.common.base.Optional;

import fi.helsinki.cs.tmc.langs.io.EverythingIsStudentFileStudentFilePolicy;
import fi.helsinki.cs.tmc.langs.io.zip.StudentFileAwareZipper;
import fi.helsinki.cs.tmc.langs.util.TaskExecutorImpl;

import net.lingala.zip4j.exception.ZipException;

import java.io.IOException;
import java.text.ParseException;

/**
 * Submit command for submitting exercises to TMC.
 */
public class Submit extends Command<SubmissionResult> {

    private ExerciseSubmitter submitter;
    private SubmissionPoller interpreter;
    private Course course;

    /**
     * Constructor for Submit command, creates the courseSubmitter.
     */
    public Submit(TmcSettings settings) {
        super(settings);
        UrlCommunicator urlComms = new UrlCommunicator(settings);
        TmcJsonParser jsonParser = new TmcJsonParser(urlComms, settings);
        interpreter = new SubmissionPoller(jsonParser);
        submitter =
                new ExerciseSubmitter(
                        new ProjectRootFinder(new TaskExecutorImpl(), new TmcJsonParser(settings)),
                        new StudentFileAwareZipper(new EverythingIsStudentFileStudentFilePolicy()),
                        new UrlCommunicator(settings),
                        new TmcJsonParser(settings),
                        settings);
    }

    /**
     * Constructor for Submit command, creates the courseSubmitter.
     * @param path path which to submit
     */
    public Submit(String path, TmcSettings settings) {
        this(settings);
        this.setParameter("path", path);
    }

    /**
     * Constructor for mocking.
     *
     * @param submitter   can inject submitter mock.
     * @param interpreter can inject interpreter mock.
     */
    public Submit(
            ExerciseSubmitter submitter,
            SubmissionPoller interpreter,
            TmcSettings settings,
            String path) {
        super(settings);
        this.setParameter("path", path);
        this.interpreter = interpreter;
        this.submitter = submitter;
    }

    /**
     * Requires auth and pwd in "path" parameter.
     *
     * @throws TmcCoreException if no auth or no path supplied.
     */
    @Override
    public void checkData() throws TmcCoreException, IOException {
        if (!settings.userDataExists()) {
            throw new TmcCoreException("User must be authorized first");
        }
        if (!this.data.containsKey("path")) {
            throw new TmcCoreException("path not supplied");
        }

        Optional<Course> currentCourse = settings.getCurrentCourse();
        if (currentCourse.isPresent()) {
            course = currentCourse.get();
        } else {
            throw new TmcCoreException("Unable to determine course");
        }
    }

    @Override
    public SubmissionResult call()
            throws TmcCoreException, IOException, ParseException, ExpiredException,
                    IllegalArgumentException, ZipException, InterruptedException {
        checkData();
        String returnUrl = submitter.submit(data.get("path"));
        SubmissionResult result = interpreter.getSubmissionResult(returnUrl);
        return result;
    }
}
