package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.TmcServerCommunicationTaskFactory;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * A {@link Command} for sending a paste to the server with an attached comment.
 */
public class PasteWithComment extends AbstractSubmissionCommand<URI> {

    private Exercise exercise;
    private String message;

    public PasteWithComment(ProgressObserver observer, Exercise exercise, String message) {
        super(observer);
        this.exercise = exercise;
        this.message = message;
    }

    @Override
    public URI call() throws Exception {

        Map<String, String> extraParams = new HashMap<>();
        extraParams.put("paste", "1");
        if (!this.message.isEmpty()) {
            extraParams.put("message_for_paste", message);
        }

        TmcServerCommunicationTaskFactory.SubmissionResponse submissionResponse
                = submitToServer(exercise, extraParams);

        return submissionResponse.pasteUrl;
    }
}
