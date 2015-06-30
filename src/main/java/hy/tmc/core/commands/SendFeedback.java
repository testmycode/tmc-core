package hy.tmc.core.commands;

import com.google.common.base.Optional;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hy.tmc.core.communication.HttpResult;
import hy.tmc.core.communication.UrlCommunicator;
import hy.tmc.core.exceptions.ProtocolException;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Map.Entry;


public class SendFeedback extends Command<HttpResult> {

    private Map<String, String> answers;
    private String url;
    
    public SendFeedback(Map<String, String> answers, String url) {
        this.answers = answers;
        this.url = url;
    }
    
    @Override
    public void checkData() throws ProtocolException, IOException {
        if (answers == null || url == null) {
            throw new ProtocolException("must give answers and feedback url");
        }
    }

    @Override
    public HttpResult call() throws Exception {
        JsonArray feedbackAnswers = new JsonArray();
        
        for (Entry<String, String> e : answers.entrySet()) {

            JsonObject jsonAnswer = new JsonObject();
            jsonAnswer.addProperty("question_id", e.getKey());
            jsonAnswer.addProperty("answer", e.getValue());
            feedbackAnswers.add(jsonAnswer);
        }
        
        JsonObject req = new JsonObject();
        req.add("answers", feedbackAnswers);

        return UrlCommunicator.makePostWithJson(req, url);
    }

    
}
