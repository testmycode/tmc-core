package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.ExerciseSubmitter;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.exceptions.ExpiredException;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;
import fi.helsinki.cs.tmc.langs.domain.NoLanguagePluginFoundException;

import com.google.common.base.Optional;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.text.ParseException;

/**
 * A {@link Command} for requesting a code review for code with a message.
 */
public class RequestCodeReview extends Command<URI> {

    private ExerciseSubmitter submitter;
    private String comment;
    private Path path;

    /**
     * Constructs a new code review request with a message.
     */
    public RequestCodeReview(TmcSettings settings, Path path, String comment) {
        this(settings, path, comment, new ExerciseSubmitter(settings));
    }

    /**
     * Constructs a new code review request with a message.
     */
    public RequestCodeReview(
            TmcSettings settings, Path path, String comment, ExerciseSubmitter submitter) {
        super(settings);

        this.submitter = submitter;
        this.path = path;
        this.comment = comment;
    }

    /**
     * Entry point for launching this command.
     */
    // TODO: exceptiom fun
    @Override
    public URI call()
            throws TmcCoreException, ExpiredException, ParseException, IOException,
                    URISyntaxException, IllegalArgumentException, NoLanguagePluginFoundException {
        // TODO: do this check elsewhere/om settigs? make settings/api to thow it, whoever uses this.
        if (!settings.userDataExists()) {
            throw new TmcCoreException("Cannot request a code review. User must be authenticated.");
        }

        Optional<Course> currentCourse = settings.getCurrentCourse();
        if (currentCourse.isPresent()) {
            return submitter.submitWithCodeReviewRequest(this.path, this.comment);
        } else {
            throw new TmcCoreException("Cannot request a code review. Unable to determine course.");
        }
    }
}
