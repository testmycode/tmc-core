package hy.tmc.cli.backend;

import com.google.common.base.Optional;
import hy.tmc.cli.frontend.communication.commands.Command;
import hy.tmc.cli.frontend.communication.server.ProtocolException;


public class BadCommandStub extends Command{

    protected Optional<String> functionality() {     
        return null;
    }

    @Override
    public void checkData() throws ProtocolException {   
        throw new ProtocolException("I'm a bad command-stub :(");
    }    

    @Override
    public Optional parseData(Object data) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object call() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
