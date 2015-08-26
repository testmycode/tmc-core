package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.ExerciseSubmitter;
import fi.helsinki.cs.tmc.core.communication.SubmissionPoller;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.domain.submission.SubmissionResult;
import fi.helsinki.cs.tmc.core.exceptions.ExpiredException;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;

/**
 * A {@link Command} for submitting an exercise to the server.
 */
public class Submit extends Command<SubmissionResult> {

    private ExerciseSubmitter submitter;
    private SubmissionPoller submissionPoller;
    private String path;

    /**
     * Creates a new submit command with {@code settings}, {@code submitter} and
     * {@code submissionPoller} for submitting the exercise located at {@code path}.
     */
    public Submit(
            TmcSettings settings, ExerciseSubmitter submitter,
            SubmissionPoller submissionPoller, String path) {
        this(settings, submitter, submissionPoller, path, null);
    }

    /**
     * Creates a new submit command with {@code settings}, {@code submitter} and
     * {@code submissionPoller} for submitting the exercise located at {@code path}. The
     * {@code observer} will be notified of submission progress.
     */
    public Submit(TmcSettings settings, ExerciseSubmitter submitter, 
            SubmissionPoller submissionPoller, String path, ProgressObserver observer) {
        super(settings);

        this.path = path;
        this.submissionPoller = submissionPoller;
        this.submitter = submitter;
        super.observer = observer;
    }

    private void assertHasRequiredData() throws TmcCoreException {
        String username = settings.getUsername();
        if (username == null || username.isEmpty()) {
            throw new TmcCoreException("username must be set!");
        }

        String password = settings.getPassword();
        if (password == null || password.isEmpty()) {
            throw new TmcCoreException("password must be set!");
        }
    }

    /**
     * Entry point for launching this command.
     */
    @Override
    public SubmissionResult call()
            throws TmcCoreException, IOException, ParseException, ExpiredException,
            IllegalArgumentException, InterruptedException, URISyntaxException {

        assertHasRequiredData();

        if (observer != null) {
            String returnUrl = submitter.submit(this.path, observer);
            return submissionPoller.getSubmissionResult(returnUrl, observer);
        }

        String returnUrl = submitter.submit(this.path);
        return submissionPoller.getSubmissionResult(returnUrl);
    }
}
