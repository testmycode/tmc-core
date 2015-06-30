package hy.tmc.core.commands;

import com.google.common.base.Optional;
import hy.tmc.core.Mailbox;
import hy.tmc.core.communication.CourseSubmitter;
import hy.tmc.core.communication.SubmissionPoller;
import hy.tmc.core.configuration.ClientData;
import hy.tmc.core.domain.Course;
import hy.tmc.core.exceptions.ExpiredException;
import hy.tmc.core.exceptions.ProtocolException;
import hy.tmc.core.synchronization.TmcServiceScheduler;

import hy.tmc.core.domain.submission.SubmissionResult;

import hy.tmc.core.zipping.DefaultRootDetector;
import hy.tmc.core.zipping.ProjectRootFinder;
import hy.tmc.core.zipping.Zipper;

import java.io.IOException;
import java.text.ParseException;

import net.lingala.zip4j.exception.ZipException;

/**
 * Submit command for submitting exercises to TMC.
 */
public class Submit extends Command<SubmissionResult> {

    private CourseSubmitter submitter;
    private SubmissionPoller interpreter;
    private Course course;
    private MailChecker mail;

    /**
     * Constructor for Submit command, creates the courseSubmitter.
     */
    public Submit() {
        submitter = new CourseSubmitter(
                new ProjectRootFinder(new DefaultRootDetector()),
                new Zipper()
        );
        mail = new MailChecker();
    }
    
     /**
     * Constructor for Submit command, creates the courseSubmitter.
     * @param path path which to submit
     */
    public Submit(String path) {
        submitter = new CourseSubmitter(
                new ProjectRootFinder(new DefaultRootDetector()),
                new Zipper()
        );
        mail = new MailChecker();
        this.setParameter("path", path);
    }

    /**
     * Constructor for mocking.
     *
     * @param submitter   can inject submitter mock.
     * @param interpreter can inject interpreter mock.
     */
    public Submit(CourseSubmitter submitter, SubmissionPoller interpreter) {
        this.interpreter = interpreter;
        this.submitter = submitter;
        mail = new MailChecker();
    }

    /**
     * Requires auth and pwd in "path" parameter.
     *
     * @throws ProtocolException if no auth or no path supplied.
     */
    @Override
    public void checkData() throws ProtocolException, IOException {
        if (!ClientData.userDataExists()) {
            throw new ProtocolException("User must be authorized first");
        }
        if (!this.data.containsKey("path")) {
            throw new ProtocolException("path not supplied");
        }

        Optional<Course> currentCourse = ClientData.getCurrentCourse(data.get("path"));
        if (currentCourse.isPresent()) {
            course = currentCourse.get();
        } else {
            throw new ProtocolException("Unable to determine course");
        }
    }

    @Override
    public SubmissionResult call() throws ProtocolException, IOException, ParseException, ExpiredException, IllegalArgumentException, ZipException, InterruptedException {
        TmcServiceScheduler.startIfNotRunning(course);
        checkData();
        String returnUrl = submitter.submit(data.get("path"));
        SubmissionResult result = interpreter.getSubmissionResult(returnUrl);
        return result;
    }
    
}
