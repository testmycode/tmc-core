package hy.tmc.core.commands;

import hy.tmc.core.commands.ChooseServer;
import hy.tmc.core.configuration.ConfigHandler;
import hy.tmc.core.exceptions.TmcCoreException;

import org.apache.commons.io.FileUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


public class ChooseServerTest {

    private ChooseServer chooser;
    private final String path = "testResources/test.properties";

    @Before
    public void setup() {
        this.chooser = new ChooseServer(new ConfigHandler(path));
    }
    
    @After
    public void teardown() {
        new File(path).delete();
    }

    @Test
    public void createNewHelp() {
        assertNotNull(chooser);
    }

    @Test
    public void testFunctionality() throws TmcCoreException, Exception {
        chooser.setParameter("tmc-server", "http://tmc.ebin.fi");
        chooser.call();
        try {
            String propFile = FileUtils.readFileToString(new File(path));
            assertTrue(propFile.contains("tmc.ebin.fi"));
        } catch (IOException ex) {
            fail("unable to read propertiesfile");
        }
    }
    
    @Test (expected = TmcCoreException.class)
    public void throwsExceptionWithoutData() throws TmcCoreException {
        chooser.checkData();
    }
    
    @Test
    public void correctUrlisAccepted() {
        chooser.setParameter("tmc-server", "http://tmc.mooc.fi");
        try {
            chooser.checkData();
        } catch (TmcCoreException ex) {
            fail("checkData threw exception");
        }
    }
    
    @Test (expected = TmcCoreException.class)
    public void incorrectUrlThrowsException() throws TmcCoreException {
        chooser.setParameter("tmc-server", "lak3jf02ja3fji23j");
        chooser.checkData();
    }
}
