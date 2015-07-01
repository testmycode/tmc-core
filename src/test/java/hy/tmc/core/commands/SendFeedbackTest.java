package hy.tmc.core.commands;

import hy.tmc.core.commands.Command;
import hy.tmc.core.commands.SendFeedback;
import com.google.common.base.Optional;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import hy.tmc.core.communication.HttpResult;
import hy.tmc.core.communication.UrlCommunicator;
import hy.tmc.core.exceptions.TmcCoreException;
import hy.tmc.core.testhelpers.ExampleJson;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(UrlCommunicator.class)
public class SendFeedbackTest {

    private SendFeedback command;
    private String url = "www.example.tmc";

    @Before
    public void setUp() {
        command = new SendFeedback(testCaseMap(), url);
        PowerMockito.mockStatic(UrlCommunicator.class);
    }

    private Map<String, String> testCaseMap() {
        Map<String, String> answers = new TreeMap();
        answers.put("4", "jee jee!");
        answers.put("13", "Oli kiva tehtävä. Opin paljon koodia, nyt tunnen osaavani paljon paremmin");
        answers.put("88", "<(^)\n (___)\n lorem ipsum, sit dolor amet");

        return answers;
    }

    @Test
    public void testCall() throws Exception {
        command.call();
        JsonParser parser = new JsonParser();
        JsonObject expectedJson = (JsonObject) parser.parse(ExampleJson.sentFeedbackExample);
        PowerMockito.verifyStatic();
        UrlCommunicator.makePostWithJson(expectedJson, url);
    }
    
    @Test (expected = TmcCoreException.class)
    public void ensureParamsNotNull() throws TmcCoreException, IOException {
        Command command = new SendFeedback(null, "chewbac.ca");
        command.checkData();
    }
    
    @Test (expected = TmcCoreException.class)
    public void ensureParamsNotNull2() throws TmcCoreException, IOException {
        Command command = new SendFeedback(new HashMap<String, String>(), null);
        command.checkData();
    }
    
    @Test (expected = TmcCoreException.class)
    public void ensureParamsNotNull3() throws TmcCoreException, IOException {
        Command command = new SendFeedback(null, null);
        command.checkData();
    }

}
