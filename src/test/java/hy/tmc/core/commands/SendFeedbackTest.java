package hy.tmc.core.commands;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import hy.tmc.core.communication.UrlCommunicator;
import hy.tmc.core.exceptions.TmcCoreException;
import hy.tmc.core.CoreTestSettings;
import hy.tmc.core.testhelpers.ExampleJson;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;


public class SendFeedbackTest {

    private SendFeedback command;
    private String url = "www.example.tmc";
    CoreTestSettings settings;
    UrlCommunicator communicator;

    @Before
    public void setUp() {
        settings = new CoreTestSettings();
        communicator = Mockito.mock(UrlCommunicator.class);
        command = new SendFeedback(testCaseMap(), url, settings);
        
    }

    private Map<String, String> testCaseMap() {
        Map<String, String> answers = new TreeMap();
        answers.put("4", "jee jee!");
        answers.put("13", "Oli kiva tehtävä. Opin paljon koodia, nyt tunnen osaavani paljon paremmin");
        answers.put("88", "<(^)\n (___)\n lorem ipsum, sit dolor amet");

        return answers;
    }
    
    @Test (expected = TmcCoreException.class)
    public void ensureParamsNotNull() throws TmcCoreException, IOException {
        settings = new CoreTestSettings();
        Command command = new SendFeedback(null, "chewbac.ca", settings);
        command.checkData();
    }
    
    @Test (expected = TmcCoreException.class)
    public void ensureParamsNotNull2() throws TmcCoreException, IOException {
        settings = new CoreTestSettings();
        Command command = new SendFeedback(new HashMap<String, String>(), null, settings);
        command.checkData();
    }
    
    @Test (expected = TmcCoreException.class)
    public void ensureParamsNotNull3() throws TmcCoreException, IOException {
        settings = new CoreTestSettings();
        Command command = new SendFeedback(null, null, settings);
        command.checkData();
    }

}
