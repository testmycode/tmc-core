
package hy.tmc.cli.frontend.communication.server;

import hy.tmc.cli.frontend.communication.commands.Command;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class ProtocolParserTest {

    private ProtocolParser parser;
    
    @Before
    public void setup() {
        this.parser = new ProtocolParser();
    }
    
    @Test
    public void testGiveDataWithVimflag() throws ProtocolException{
        Command command = parser.getCommand("submit path /asd/lmao --vim");
        Assert.assertNotNull(command.getData().get("--vim"));
    }
}
