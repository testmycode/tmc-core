import org.junit.Test;
import org.junit.Rule;

import static org.junit.Assert.*;
import fi.helsinki.cs.tmc.edutestutils.Points;
import fi.helsinki.cs.tmc.edutestutils.MockStdio;

@Points("1")
public class NimiTest {
    @Rule
    public MockStdio io = new MockStdio();

    @Test
    public void test() {
        Nimi.main(new String[0]);
        String out = io.getSysOut();
        assertTrue("Et tulostanut mitään!",out.length()>0);
    }
}