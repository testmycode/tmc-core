package feature.setserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import hy.tmc.cli.configuration.ConfigHandler;
import hy.tmc.cli.frontend.communication.commands.ChooseServer;
import hy.tmc.cli.frontend.communication.server.ProtocolException;

import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import java.io.IOException;

public class SetServerSteps {
    
    private ConfigHandler handler;
    private ChooseServer command;
    private String origServer;
    private String output;
    
    /**
     * Set up confighandler, frontend stub & choose server command.
     */
    @Before
    public void setup() {
        handler = new ConfigHandler("testResources/test.properties");
        command = new ChooseServer(handler);
    }

    @Given ("^the server is \"(.*)\"$")
    public void stagingServerSelected(String server) throws IOException {
        origServer = server;
        handler.writeServerAddress(origServer);
    }
    
    /**
     * User sets server name.
     * @param serverName server name
     */
    @When ("^the user changes the server to \"(.*)\"$")
    public void serverChanged(String serverName) throws Exception {
        try {
            command.setParameter("tmc-server", serverName);
            command.checkData();
            output = command.parseData(command.call()).get();
        } catch (ProtocolException ex) {
            System.err.println(ex.getMessage());
        }
    }
    
    /**
     * User user command without parameters.
     */
    @When ("^the user uses the command without parameters$")
    public void noParamsGiven() throws Exception {
        try {
            command.checkData();
            output = command.parseData(command.call()).get();
        } catch (ProtocolException ex) {
            return;
        }
        fail("ProtocolException.");
    }
    
    @Then ("^the server will be \"(.*)\"$")
    public void correctChanges(String server) throws IOException {
        String addressInConf = handler.readServerAddress();
        assertEquals(server, addressInConf);
    }
    
    @Then ("^the server is unchanged$")
    public void noChanges() throws IOException {
        String addressInConf = handler.readServerAddress();
        assertEquals(origServer, addressInConf);
    }
}
