package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.TmcServerCommunicationTaskFactory;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;

import com.google.common.annotations.VisibleForTesting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * A {@link Command} for sending a paste to the server with an attached comment.
 */
public class PasteWithComment extends AbstractSubmissionCommand<URI> {

    private static final Logger logger = LoggerFactory.getLogger(PasteWithComment.class);

    private Exercise exercise;
    private String message;

    public PasteWithComment(ProgressObserver observer, Exercise exercise, String message) {
        super(observer);
        this.exercise = exercise;
        this.message = message;
    }

    @VisibleForTesting
    PasteWithComment(
            ProgressObserver observer,
            Exercise exercise,
            String message,
            TmcServerCommunicationTaskFactory tmcServerCommunicationTaskFactory) {
        super(observer, tmcServerCommunicationTaskFactory);
        this.exercise = exercise;
        this.message = message;
    }

    @Override
    public URI call() throws Exception {
        logger.info("Creating a TMC paste");
        informObserver(0, "Creating a TMC paste");

        Map<String, String> extraParams = new HashMap<>();
        extraParams.put("paste", "1");
        if (!this.message.isEmpty()) {
            extraParams.put("message_for_paste", message);
        }

        TmcServerCommunicationTaskFactory.SubmissionResponse submissionResponse =
                submitToServer(exercise, extraParams);

        logger.debug("Successfully created TMC paste");
        informObserver(1, "Successfully created TMC paste");

        return submissionResponse.pasteUrl;
    }
}
