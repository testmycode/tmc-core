package hy.tmc.core.commands;

import hy.tmc.core.configuration.ClientData;
import hy.tmc.core.exceptions.TmcCoreException;

/**
 * Allows the user to log out.
 */
public class Logout extends Command<Boolean> {

    @Override
    public void checkData() throws TmcCoreException {}

    @Override
    public Boolean call() throws TmcCoreException {
        if (ClientData.userDataExists()) {
            ClientData.clearUserData();
            return true;
        } else {
            return false;
        }
    }
}
