package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.TmcServerCommunicationTaskFactory;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.domain.submission.FeedbackAnswer;

import com.google.gson.JsonParser;

import java.net.URI;
import java.util.List;

/**
 * A {@link Command} for sending user feedback to the server.
 */
public class SendFeedback extends Command<Boolean> {

    private List<FeedbackAnswer> answers;
    private URI feedbackUri;

    public SendFeedback(ProgressObserver observer, List<FeedbackAnswer> answers, URI feedbackUri) {
        super(observer);
        this.answers = answers;
        this.feedbackUri = feedbackUri;
    }

    @Override
    public Boolean call() throws Exception {

        String response = new TmcServerCommunicationTaskFactory().getFeedbackAnsweringJob(
                //TODO: Str -> URI
                feedbackUri.toString(),
                answers
        ).call();

        return respondedSuccessfully(response);

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
