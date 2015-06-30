package hy.tmc.core.commands;


import hy.tmc.core.commands.DownloadExercises;
import hy.tmc.core.configuration.ClientData;
import hy.tmc.core.exceptions.ProtocolException;
import org.junit.Before;
import org.junit.Test;

public class DownloadExercisesTest {

    @Before
    public void setup() {
        ClientData.setUserData("Bossman", "Samu");
    }
    
    /**
     * Check that data checking success.
     */
    @Test
    public void testCheckDataSuccess() throws ProtocolException {
        ClientData.setUserData("mister", "Kristian");
        DownloadExercises de = new DownloadExercises();
        de.setParameter("path", "/home/tmccli/uolevipuistossa");
        de.setParameter("courseID", "21");
        de.checkData();
        ClientData.clearUserData();
    }

    /**
     * Check that if user didn't give correct data, data checking fails.
     */
    @Test(expected = ProtocolException.class)
    public void testCheckDataFail() throws ProtocolException {
        DownloadExercises de = new DownloadExercises();
        de.checkData();
    }

    /**
     * User gives course id that isn't a number and will be informed about it.
     */
    @Test(expected = ProtocolException.class)
    public void courseIdNotANumber() throws ProtocolException {
        DownloadExercises de = new DownloadExercises();
        de.setParameter("path", "/home/tmccli/uolevipuistossa");
        de.setParameter("courseID", "not a number");
        de.checkData();
    }
}
