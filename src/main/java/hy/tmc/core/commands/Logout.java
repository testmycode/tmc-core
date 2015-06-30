package hy.tmc.core.commands;

import hy.tmc.core.configuration.ClientData;
import hy.tmc.core.exceptions.ProtocolException;

/**
 * Allows the user to log out.
 */
public class Logout extends Command<Boolean> {

    @Override
    public void checkData() throws ProtocolException {}

    @Override
    public Boolean call() throws ProtocolException {
        if (ClientData.userDataExists()) {
            ClientData.clearUserData();
            return true;
        } else {
            return false;
        }
    }
}
