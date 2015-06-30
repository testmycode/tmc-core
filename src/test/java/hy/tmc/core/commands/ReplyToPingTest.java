package hy.tmc.core.commands;

import hy.tmc.core.commands.ReplyToPing;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ReplyToPingTest {

    @Test
    public void pingAnswersAsPong() {
        ReplyToPing ping = new ReplyToPing();
        assertEquals("pong", ping.call());
    }
}
