package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.HttpResult;
import fi.helsinki.cs.tmc.core.communication.UrlCommunicator;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.Map;
import java.util.Map.Entry;

/**
 * A {@link Command} for sending user feedback to the server.
 */
public class SendFeedback extends Command<HttpResult> {

    private Map<String, String> answers;
    private String url;

    /**
     * Constructs a send feedback command with {@code settings} for sending {@code answers} to
     * {@code url}.
     */
    public SendFeedback(TmcSettings settings, Map<String, String> answers, String url) {
        super(settings);
        this.answers = answers;
        this.url = url;
    }

    /**
     * Entry point for launching this command.
     */
    @Override
    public HttpResult call() throws Exception {
        JsonArray feedbackAnswers = new JsonArray();

        for (Entry<String, String> entry : answers.entrySet()) {
            JsonObject jsonAnswer = new JsonObject();
            jsonAnswer.addProperty("question_id", entry.getKey());
            jsonAnswer.addProperty("answer", entry.getValue());
            feedbackAnswers.add(jsonAnswer);
        }

        JsonObject jsonParent = new JsonObject();
        jsonParent.add("answers", feedbackAnswers);

        return new UrlCommunicator(settings).makePostWithJson(jsonParent, url);
    }
}
