package hy.tmc.cli.frontend.communication.commands;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import hy.tmc.core.exceptions.ProtocolException;

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
}
