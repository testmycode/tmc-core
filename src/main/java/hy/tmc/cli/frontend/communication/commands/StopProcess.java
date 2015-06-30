package hy.tmc.cli.frontend.communication.commands;

import com.google.common.base.Optional;
import hy.tmc.cli.frontend.FrontendListener;
import hy.tmc.cli.frontend.communication.server.ProtocolException;


public class StopProcess extends Command<String> {

    /**
     * Exit java virtual machine
     */
    protected Optional<String> functionality() {
        System.exit(0);
        return Optional.absent();
    }

    /**
     * Does nothing, this command does not require data.
     * @throws ProtocolException 
     */
    @Override
    public void checkData() throws ProtocolException {
    }

    @Override
    public Optional<String> parseData(Object data) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String call() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
