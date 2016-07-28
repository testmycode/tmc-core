package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.TmcServerCommunicationTaskFactory;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * A {@link Command} for requesting a code review for code with a message.
 */
public class RequestCodeReview extends AbstractSubmissionCommand<TmcServerCommunicationTaskFactory.SubmissionResponse> {

    private static final Logger logger = LoggerFactory.getLogger(RequestCodeReview.class);
    private final Exercise exercise;
    private final String message;

    public RequestCodeReview(ProgressObserver observer, Exercise exercise, String message) {
        super(observer);
        this.exercise = exercise;
        this.message = message;
    }

    @Override
    public TmcServerCommunicationTaskFactory.SubmissionResponse call() throws Exception {
        logger.info("Creating a TMC paste");
        informObserver(0, "Requesting review");

        Map<String, String> extraParams = new HashMap<>();

        extraParams.put("request_review", "1");
        if (!Strings.isNullOrEmpty(message)) {
            extraParams.put("message_for_reviewer", message);
        }

        TmcServerCommunicationTaskFactory.SubmissionResponse submissionResponse =
            submitToServer(exercise, extraParams);

        logger.debug("Successfully requested review");
        informObserver(1, "Successfully requested review");

        return submissionResponse;
    }
}
