package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.ExerciseSubmitter;
import fi.helsinki.cs.tmc.core.communication.TmcJsonParser;
import fi.helsinki.cs.tmc.core.communication.UrlCommunicator;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.exceptions.ExpiredException;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;
import fi.helsinki.cs.tmc.core.zipping.ProjectRootFinder;

import com.google.common.base.Optional;

import fi.helsinki.cs.tmc.langs.io.EverythingIsStudentFileStudentFilePolicy;
import fi.helsinki.cs.tmc.langs.io.zip.StudentFileAwareZipper;
import fi.helsinki.cs.tmc.langs.util.TaskExecutorImpl;

import net.lingala.zip4j.exception.ZipException;

import java.io.IOException;
import java.net.URI;
import java.text.ParseException;

public class PasteWithComment extends Command<URI> {

    private ExerciseSubmitter submitter;
    private Course course;
    private String comment;

    /**
     * Submit paste with comment. Used in tmc-netbeans plugin.
     * @param comment paste comment given by user
     */
    public PasteWithComment(TmcSettings settings, String comment) {
        this(
                new ExerciseSubmitter(
                        new ProjectRootFinder(new TaskExecutorImpl(), new TmcJsonParser(settings)),
                        new StudentFileAwareZipper(new EverythingIsStudentFileStudentFilePolicy()),
                        new UrlCommunicator(settings),
                        new TmcJsonParser(settings),
                        settings),
                settings,
                comment);
    }

    /**
     * Constructor for mocking.
     *
     * @param submitter can inject submitter mock.
     */
    public PasteWithComment(ExerciseSubmitter submitter, TmcSettings settings, String comment) {
        this.submitter = submitter;
        this.settings = settings;
        this.comment = comment;
    }

    public PasteWithComment(String path, TmcSettings settings, String comment) {
        this(settings, comment);
        this.setParameter("path", path);
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

    /**
     * Takes a pwd command's output in "path" and prints out the URL for the
     * paste.
     */
    @Override
    public URI call()
            throws IOException, ParseException, ExpiredException, IllegalArgumentException,
                    ZipException, TmcCoreException {
        checkData();
        URI uri = URI.create(submitter.submitPasteWithComment(data.get("path"), this.comment));
        return uri;
    }
}
