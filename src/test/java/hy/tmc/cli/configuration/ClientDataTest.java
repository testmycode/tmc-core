package hy.tmc.cli.configuration;

import static hy.tmc.cli.configuration.ClientData.getFormattedUserData;
import static hy.tmc.cli.configuration.ClientData.getPassword;
import static hy.tmc.cli.configuration.ClientData.getUsername;
import static hy.tmc.cli.configuration.ClientData.logOutCurrentUser;
import static hy.tmc.cli.configuration.ClientData.setUserData;
import static hy.tmc.cli.configuration.ClientData.userDataExists;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


import org.junit.Before;
import org.junit.Test;

public class ClientDataTest {

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
     * Test of setUserData method, of class ClientData.
     */
    @Test
    public final void testSetUserData() {
        String username = "";
        String password = "";
        ClientData.setUserData(username, password);
        assertEquals("", ClientData.getUsername());
    }

    /**
     * Test of userDataExists method, of class ClientData.
     */
    @Test
    public void testUserDataExists() {
        boolean expResult = false;
        boolean result = ClientData.userDataExists();
        assertEquals(expResult, result);
    }

    /**
     * Test of clearUserData method, of class ClientData.
     */
    @Test
    public void testClearUserData() {
        ClientData.clearUserData();
        assertFalse(ClientData.userDataExists());
    }

    /**
     * Test of getFormattedUserData method, of class ClientData.
     */
    @Test
    public void testGetFormattedUserData() {
        String expResult = ":";
        String result = ClientData.getFormattedUserData();
        assertEquals(expResult, result);
    }

    /**
     * Test of logOutCurrentUser method, of class ClientData.
     */
    @Test
    public void testLogOutCurrentUser() {
        ClientData.logOutCurrentUser();
        assertFalse(ClientData.userDataExists());
    }

    /**
     * Test of getPID method, of class ClientData.
     */
    @Test
    public void testGetPid() {
        int expResult = 0;
        int result = ClientData.getPid();
        assertEquals(expResult, result);
    }

    /**
     * Test of setPID method, of class ClientData.
     */
    @Test
    public void testSetPid() {
        int pid = 123;
        ClientData.setPid(pid);
        assertEquals(pid, ClientData.getPid());
    }

    /**
     * Test of getUSERNAME method, of class ClientData.
     */
    @Test
    public void testGetUsername() {
        String expResult = "";
        String result = ClientData.getUsername();
        assertEquals(expResult, result);
    }

    /**
     * Test of getPASSWORD method, of class ClientData.
     */
    @Test
    public void testGetPassword() {
        String expResult = "";
        String result = ClientData.getPassword();
        assertEquals(expResult, result);
    }

}
