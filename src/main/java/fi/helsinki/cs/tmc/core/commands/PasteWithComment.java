package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.ExerciseSubmitter;
import fi.helsinki.cs.tmc.core.communication.TmcApi;
import fi.helsinki.cs.tmc.core.communication.UrlCommunicator;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.exceptions.ExpiredException;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;
import fi.helsinki.cs.tmc.core.zipping.ProjectRootFinder;
import fi.helsinki.cs.tmc.langs.io.EverythingIsStudentFileStudentFilePolicy;
import fi.helsinki.cs.tmc.langs.io.zip.StudentFileAwareZipper;
import fi.helsinki.cs.tmc.langs.util.TaskExecutorImpl;

import com.google.common.base.Optional;

import net.lingala.zip4j.exception.ZipException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;

public class PasteWithComment extends Command<URI> {

    private ExerciseSubmitter submitter;
    private Course course;
    private String comment;
    private String path;

    /**
     * Submit paste with comment. Used in tmc-netbeans plugin.
     * @param comment paste comment given by user
     */
    public PasteWithComment(TmcSettings settings, String path, String comment) {
        this(
                new ExerciseSubmitter(
                        new ProjectRootFinder(new TaskExecutorImpl(), new TmcApi(settings)),
                        new StudentFileAwareZipper(new EverythingIsStudentFileStudentFilePolicy()),
                        new UrlCommunicator(settings),
                        new TmcApi(settings),
                        settings),
                settings,
                path,
                comment);
    }

    /**
     * Constructor for mocking.
     *
     * @param submitter can inject submitter mock.
     */
    public PasteWithComment(
            ExerciseSubmitter submitter, TmcSettings settings, String path, String comment) {
        this.submitter = submitter;
        this.settings = settings;
        this.path = path;
        this.comment = comment;
    }

    /**
     * Takes a pwd command's output in "path" and prints out the URL for the
     * paste.
     */
    @Override
    public URI call()
            throws IOException, ParseException, ExpiredException, ZipException, TmcCoreException, URISyntaxException {
        if (!settings.userDataExists()) {
            throw new TmcCoreException("User must be authenticated");
        }

        Optional<Course> currentCourse = settings.getCurrentCourse();
        if (currentCourse.isPresent()) {
            course = currentCourse.get();
        } else {
            throw new TmcCoreException("Unable to determine course");
        }

        return URI.create(submitter.submitPasteWithComment(this.path, this.comment));
    }
}
