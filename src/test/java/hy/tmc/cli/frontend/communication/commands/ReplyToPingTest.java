package hy.tmc.cli.frontend.communication.commands;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ReplyToPingTest {

    @Test
    public void pingAnswersAsPong() {
        ReplyToPing ping = new ReplyToPing();
        assertEquals("pong", ping.call());
    }
}
