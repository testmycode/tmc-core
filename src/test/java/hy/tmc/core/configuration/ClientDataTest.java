package hy.tmc.core.configuration;

import hy.tmc.core.configuration.ClientTmcSettings;
import static hy.tmc.core.configuration.ClientTmcSettings.getFormattedUserData;
import static hy.tmc.core.configuration.ClientTmcSettings.getPassword;
import static hy.tmc.core.configuration.ClientTmcSettings.getUsername;
import static hy.tmc.core.configuration.ClientTmcSettings.logOutCurrentUser;
import static hy.tmc.core.configuration.ClientTmcSettings.setUserData;
import static hy.tmc.core.configuration.ClientTmcSettings.userDataExists;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


import org.junit.Before;
import org.junit.Test;

public class ClientTmcSettingsTest {

    @Before
    public void setUp() {
        logOutCurrentUser();
    }

    @Test
    public void initiallyUserDataIsInitialData() {
        String st = getUsername() + getPassword();
        assertTrue(st.isEmpty());
    }

    @Test
    public void afterLoginInNewDataIsSet() {
        setUserData("ASD", "DSA");
        assertEquals("ASD", getUsername());
    }

    @Test
    public void logOutResetsUserData() {
        setUserData("ASD", "DSA");
        logOutCurrentUser();
        assertEquals("", getUsername());
    }

    @Test
    public void formatFormatsTheDataRight() {
        setUserData("ASD", "DSA");
        assertEquals("ASD:DSA", getFormattedUserData());
    }

    @Test
    public void userDataExistsAfterSet() {
        setUserData("ASD", "DSA");
        assertTrue(userDataExists());
    }

    /**
     * Test of setUserData method, of class ClientTmcSettings.
     */
    @Test
    public final void testSetUserData() {
        String username = "";
        String password = "";
        ClientTmcSettings.setUserData(username, password);
        assertEquals("", ClientTmcSettings.getUsername());
    }

    /**
     * Test of userDataExists method, of class ClientTmcSettings.
     */
    @Test
    public void testUserDataExists() {
        boolean expResult = false;
        boolean result = ClientTmcSettings.userDataExists();
        assertEquals(expResult, result);
    }

    /**
     * Test of clearUserData method, of class ClientTmcSettings.
     */
    @Test
    public void testClearUserData() {
        ClientTmcSettings.clearUserData();
        assertFalse(ClientTmcSettings.userDataExists());
    }

    /**
     * Test of getFormattedUserData method, of class ClientTmcSettings.
     */
    @Test
    public void testGetFormattedUserData() {
        String expResult = ":";
        String result = ClientTmcSettings.getFormattedUserData();
        assertEquals(expResult, result);
    }

    /**
     * Test of logOutCurrentUser method, of class ClientTmcSettings.
     */
    @Test
    public void testLogOutCurrentUser() {
        ClientTmcSettings.logOutCurrentUser();
        assertFalse(ClientTmcSettings.userDataExists());
    }

    /**
     * Test of getPID method, of class ClientTmcSettings.
     */
    @Test
    public void testGetPid() {
        int expResult = 0;
        int result = ClientTmcSettings.getPid();
        assertEquals(expResult, result);
    }

    /**
     * Test of setPID method, of class ClientTmcSettings.
     */
    @Test
    public void testSetPid() {
        int pid = 123;
        ClientTmcSettings.setPid(pid);
        assertEquals(pid, ClientTmcSettings.getPid());
    }

    /**
     * Test of getUSERNAME method, of class ClientTmcSettings.
     */
    @Test
    public void testGetUsername() {
        String expResult = "";
        String result = ClientTmcSettings.getUsername();
        assertEquals(expResult, result);
    }

    /**
     * Test of getPASSWORD method, of class ClientTmcSettings.
     */
    @Test
    public void testGetPassword() {
        String expResult = "";
        String result = ClientTmcSettings.getPassword();
        assertEquals(expResult, result);
    }

}
