package hy.tmc.cli.frontend.communication.commands;

import com.google.common.base.Optional;
import hy.tmc.cli.backend.communication.CourseSubmitter;
import hy.tmc.cli.configuration.ClientData;

import hy.tmc.cli.domain.Course;
import hy.tmc.cli.frontend.communication.server.ExpiredException;
import hy.tmc.cli.frontend.communication.server.ProtocolException;
import hy.tmc.cli.synchronization.TmcServiceScheduler;
import hy.tmc.cli.zipping.DefaultRootDetector;
import hy.tmc.cli.zipping.ProjectRootFinder;
import hy.tmc.cli.zipping.Zipper;
import net.lingala.zip4j.exception.ZipException;

import java.io.IOException;
import java.net.URI;
import java.text.ParseException;

public class Paste extends Command<URI> {

    private CourseSubmitter submitter;
    private Course course;
    private MailChecker mail;

    public Paste() {
        this(new CourseSubmitter(new ProjectRootFinder(new DefaultRootDetector()), new Zipper()));
    }

    /**
     * Constructor for mocking.
     *
     * @param submitter can inject submitter mock.
     */
    public Paste(CourseSubmitter submitter) {
        this.submitter = submitter;
        this.mail = new MailChecker();
    }

    public Paste(String path) {
        this(new CourseSubmitter(new ProjectRootFinder(new DefaultRootDetector()), new Zipper()));
        this.setParameter("path", path);
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
    public Optional<String> parseData(Object data) throws IOException {
        String mail = checkMail();
        URI returnURI = (URI) data;
        return Optional.of(mail + "\n"+"Paste submitted. Here it is: \n  " + returnURI);
    }

    /**
     * Takes a pwd command's output in "path" and prints out the URL for the
     * paste.
     *
     * @return
     * @throws java.io.IOException
     * @throws java.text.ParseException
     * @throws hy.tmc.cli.frontend.communication.server.ExpiredException
     * @throws net.lingala.zip4j.exception.ZipException
     * @throws hy.tmc.cli.frontend.communication.server.ProtocolException
     */
    @Override
    public URI call() throws IOException, ParseException, ExpiredException, IllegalArgumentException, ZipException, ProtocolException {
        TmcServiceScheduler.startIfNotRunning(course);
        checkData();
        URI uri = URI.create(submitter.submitPaste(data.get("path")));
        return uri;
    }

    /**
     * HUOM EXTRAKTOI TÄMÄ OMAAN LUOKKAAN
     * Executes the mail command with necessary params.
     * Gives the mail command either a courseID (preferably) or a path
     * for determining which courses reviews and updates should be fetched.
     *
     * @throws ProtocolException if unable to find necessary params.
     */
    private String checkMail() throws IOException {
        if (data.containsKey("courseID")) {
            mail.setParameter("courseID", data.get("courseID"));
        } else if (data.containsKey("path")) {
            mail.setParameter("path", data.get("path"));
        } else {
            return "must specify path";
        }
        try {
            return mail.call();
        } catch (ProtocolException e) {
            return e.getMessage();
        }
    }
}
