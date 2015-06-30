package hy.tmc.cli.frontend.communication.commands;

import hy.tmc.cli.backend.Mailbox;
import com.google.common.base.Optional;
import hy.tmc.cli.configuration.ClientData;
import hy.tmc.core.exceptions.ProtocolException;
import hy.tmc.cli.synchronization.TmcServiceScheduler;

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
