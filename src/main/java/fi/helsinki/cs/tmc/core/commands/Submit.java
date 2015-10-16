package fi.helsinki.cs.tmc.core.commands;

import com.google.common.base.Strings;
import fi.helsinki.cs.tmc.core.communication.ExerciseSubmitter;
import fi.helsinki.cs.tmc.core.communication.SubmissionPoller;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.domain.submission.SubmissionResult;
import fi.helsinki.cs.tmc.core.exceptions.ExpiredException;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;
import fi.helsinki.cs.tmc.langs.domain.NoLanguagePluginFoundException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.text.ParseException;

/**
 * A {@link Command} for submitting an exercise to the server.
 */
public class Submit extends Command<SubmissionResult> {

    private ExerciseSubmitter submitter;
    private SubmissionPoller submissionPoller;
    private Path path;

    /**
     * Creates a new submit command with {@code settings}, {@code submitter} and
     * {@code submissionPoller} for submitting the exercise located at {@code path}.
     */
    public Submit(
            TmcSettings settings,
            ExerciseSubmitter submitter,
            SubmissionPoller submissionPoller,
            Path path) {
        this(settings, submitter, submissionPoller, path, null);
    }

    /**
     * Creates a new submit command with {@code settings}, {@code submitter} and
     * {@code submissionPoller} for submitting the exercise located at {@code path}. The
     * {@code observer} will be notified of submission progress.
     */
    public Submit(
            TmcSettings settings,
            ExerciseSubmitter submitter,
            SubmissionPoller submissionPoller,
            Path path,
            ProgressObserver observer) {
        super(settings);

        this.path = path;
        this.submissionPoller = submissionPoller;
        this.submitter = submitter;
        super.observer = observer;
    }

    private void assertHasRequiredData() throws TmcCoreException {
        checkUsername();
        checkPassword();
    }

    private void checkPassword() throws TmcCoreException {
        if (isBadString(settings.getPassword())) {
            throw new TmcCoreException("password must be set!");
        }
    }

    private void checkUsername() throws TmcCoreException {
        if (isBadString(settings.getUsername())) {
            throw new TmcCoreException("username must be set!");
        }
    }

    private boolean isBadString(String toBeTested) {
        return Strings.isNullOrEmpty(toBeTested);
    }

    /**
     * Entry point for launching this command.
     */
    @Override
    public SubmissionResult call()
            throws TmcCoreException, IOException, ParseException, ExpiredException,
                    IllegalArgumentException, InterruptedException, URISyntaxException,
                    NoLanguagePluginFoundException {

        assertHasRequiredData();

        if (observer != null) {
            URI returnUrl = submitter.submit(this.path, observer);
            return submissionPoller.getSubmissionResult(returnUrl, observer);
        }

        URI returnUrl = submitter.submit(this.path);
        return submissionPoller.getSubmissionResult(returnUrl);
    }
}
