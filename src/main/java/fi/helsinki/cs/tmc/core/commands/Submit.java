package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.ExerciseSubmitter;
import fi.helsinki.cs.tmc.core.communication.SubmissionPoller;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.submission.SubmissionResult;
import fi.helsinki.cs.tmc.core.exceptions.ExpiredException;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;

import net.lingala.zip4j.exception.ZipException;

import java.io.IOException;
import java.text.ParseException;

public class Submit extends Command<SubmissionResult> {

    private ExerciseSubmitter submitter;
    private SubmissionPoller submissionPoller;
    private String path;

    public Submit(
            ExerciseSubmitter submitter,
            SubmissionPoller submissionPoller,
            TmcSettings settings,
            String path) {
        super(settings);

        this.path = path;
        this.submissionPoller = submissionPoller;
        this.submitter = submitter;
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

    @Override
    public SubmissionResult call()
            throws TmcCoreException, IOException, ParseException, ExpiredException,
                    IllegalArgumentException, ZipException, InterruptedException {

        assertHasRequiredData();

        String returnUrl = submitter.submit(this.path);
        return submissionPoller.getSubmissionResult(returnUrl);
    }
}
