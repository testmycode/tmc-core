package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.ExerciseSubmitter;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.exceptions.ExpiredException;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;

import com.google.common.base.Optional;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;

/**
 * A {@link Command} for sending a paste to the server with an attached comment.
 */
public class PasteWithComment extends Command<URI> {

    private ExerciseSubmitter submitter;
    private String comment;
    private String path;

    /**
     * Constructs a new paste with comment command using {@code settings} for creating a paste of
     * the exercise at {@code path} with an accompanying {@code comment}.
     */
    public PasteWithComment(TmcSettings settings, String path, String comment) {
        this(settings, path, comment, new ExerciseSubmitter(settings));
    }

    /**
     * Constructs a new paste with comment command for using {@code submitter} to send a paste of
     * the exercise at {@code path} with an accompanying {@code comment} to the server.
     */
    public PasteWithComment(
            TmcSettings settings, String path, String comment, ExerciseSubmitter submitter) {
        super(settings);

        this.submitter = submitter;
        this.path = path;
        this.comment = comment;
    }

    /**
     * Entry point for launching this command.
     */
    @Override
    public URI call()
            throws TmcCoreException, ExpiredException, ParseException, IOException,
            URISyntaxException {
        if (!settings.userDataExists()) {
            throw new TmcCoreException("User must be authenticated");
        }

        Optional<Course> currentCourse = settings.getCurrentCourse();
        if (currentCourse.isPresent()) {
            return URI.create(submitter.submitPasteWithComment(this.path, this.comment));
        } else {
            throw new TmcCoreException("Unable to determine course");
        }
    }
}
