package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.TmcServerCommunicationTaskFactory;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;

import java.net.URI;


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

        ImmutableMap.Builder extraParams = ImmutableMap.builder();
        extraParams.put("paste", "1");
        if (!this.message.isEmpty()) {
            extraParams.put("message_for_paste", message);
        }
        TmcServerCommunicationTaskFactory.SubmissionResponse submissionResponse =
                submitToServer(exercise, extraParams.build());

        return submissionResponse.pasteUrl;
    }
}
