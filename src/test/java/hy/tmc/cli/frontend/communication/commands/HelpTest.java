package hy.tmc.cli.frontend.communication.commands;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import hy.tmc.cli.frontend.communication.server.ProtocolException;

import org.junit.Before;
import org.junit.Test;

public class HelpTest {

    private Help help;

    @Before
    public void setup() {
        this.help = new Help();
    }

    @Test
    public void createNewHelp() {
        assertNotNull(help);
    }

    @Test
    public void outputWorks() throws ProtocolException, Exception {
        String output = help.parseData(help.call()).get();
        assertTrue(output.contains("help"));
        assertTrue(output.contains("login"));
    }
    
    @Test
    public void outputHasParameters() throws ProtocolException, Exception {
        String output = help.parseData(help.call()).get();
        assertTrue(output.contains("<course ID>"));
        assertTrue(output.contains("<tmc-server address>"));
    }
}
