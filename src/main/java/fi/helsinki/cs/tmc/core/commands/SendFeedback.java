package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.TmcServerCommunicationTaskFactory;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.domain.submission.FeedbackAnswer;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.JsonParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;

/**
 * A {@link Command} for sending user feedback to the server.
 */
public class SendFeedback extends Command<Boolean> {

    private static final Logger logger = LoggerFactory.getLogger(SendFeedback.class);

    private List<FeedbackAnswer> answers;
    private URI feedbackUri;

    public SendFeedback(ProgressObserver observer, List<FeedbackAnswer> answers, URI feedbackUri) {
        super(observer);
        this.answers = answers;
        this.feedbackUri = feedbackUri;
    }

    @VisibleForTesting
    SendFeedback(
            ProgressObserver observer,
            List<FeedbackAnswer> answers,
            URI feedbackUri,
            TmcServerCommunicationTaskFactory tmcServerCommunicationTaskFactory) {
        super(observer, tmcServerCommunicationTaskFactory);
        this.answers = answers;
        this.feedbackUri = feedbackUri;
    }

    @Override
    public Boolean call() throws Exception {
        logger.info("Sending feedback answers");
        informObserver(0, "Sending feedback answers");

        String response =
                tmcServerCommunicationTaskFactory
                        .getFeedbackAnsweringJob(feedbackUri, answers)
                        .call();

        if (respondedSuccessfully(response)) {
            logger.debug("Successfully sent feedback");
            informObserver(1, "Feedback submitted");
            return true;
        } else {
            logger.debug(
                    "Failed to send feedback, server responded with {}",
                    response
            );
            informObserver(1, "Failed to submit feedback");
            return false;
        }
    }

    private boolean respondedSuccessfully(String response) {
        return new JsonParser()
                .parse(response)
                .getAsJsonObject()
                .get("status")
                .getAsString()
                .equals("ok");
    }
}
