package hy.tmc.core.commands;

import hy.tmc.core.commands.Logout;
import com.google.common.base.Optional;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import hy.tmc.core.configuration.ClientData;
import hy.tmc.core.exceptions.ProtocolException;

import org.junit.Before;
import org.junit.Test;

public class LogoutTest {

    private Logout logout;

    @Before
    public void setup() {
        logout = new Logout();
    }

    @Test
    public void clearsUserData() throws ProtocolException {
        ClientData.setUserData("Chang", "Samu");
        logout.call();
        assertFalse(ClientData.userDataExists());
    }

}
