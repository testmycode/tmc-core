package hy.tmc.cli.frontend.communication.commands;

import com.google.common.base.Optional;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import hy.tmc.cli.backend.communication.HttpResult;
import hy.tmc.cli.backend.communication.UrlCommunicator;
import hy.tmc.cli.frontend.communication.server.ProtocolException;
import hy.tmc.cli.testhelpers.ExampleJson;
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
    
    @Test
    public void testParseDataWithSuccess() throws IOException {
        String expected = "Feedback answers sent succesfully";
        Optional<String> result = command.parseData(new HttpResult("{status:ok}", 200, true));
        assertTrue(result.isPresent());
        assertEquals(expected, result.get());
    }    
    
    @Test
    public void testParseDataWithFail() throws IOException {
        String expected = "Sending feedbackanswers failed";
        Optional<String> result = command.parseData(new HttpResult("{status:fail}", 200, true));
        assertTrue(result.isPresent());
        assertEquals(expected, result.get());
    }
    
    @Test (expected = ProtocolException.class)
    public void ensureParamsNotNull() throws ProtocolException, IOException {
        Command command = new SendFeedback(null, "chewbac.ca");
        command.checkData();
    }
    
    @Test (expected = ProtocolException.class)
    public void ensureParamsNotNull2() throws ProtocolException, IOException {
        Command command = new SendFeedback(new HashMap<String, String>(), null);
        command.checkData();
    }
    
    @Test (expected = ProtocolException.class)
    public void ensureParamsNotNull3() throws ProtocolException, IOException {
        Command command = new SendFeedback(null, null);
        command.checkData();
    }

}
