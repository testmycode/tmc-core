package hy.tmc.core.commands;

import hy.tmc.core.Mailbox;
import com.google.common.base.Optional;
import hy.tmc.core.configuration.ClientData;
import hy.tmc.core.exceptions.ProtocolException;
import hy.tmc.core.synchronization.TmcServiceScheduler;

/**
 * Allows the user to log out.
 */
public class Logout extends Command<Boolean> {

    @Override
    public void checkData() throws ProtocolException {}

    @Override
    public Boolean call() throws ProtocolException {
        if (ClientData.userDataExists()) {
            TmcServiceScheduler.getScheduler().stop();
            Mailbox.destroy();
            ClientData.clearUserData();
            return true;
        } else {
            return false;
        }
    }
}
