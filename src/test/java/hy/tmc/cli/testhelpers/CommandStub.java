package hy.tmc.cli.testhelpers;

import com.google.common.base.Optional;
import hy.tmc.cli.frontend.communication.commands.Command;
import hy.tmc.core.exceptions.ProtocolException;

//To change return value of this command, change class definition
public class CommandStub extends Command<String> {

    protected Optional<String> functionality() {     
        return Optional.absent();
    }

    @Override
    public void checkData() throws ProtocolException {        
    }    


    @Override
    public String call() throws Exception {
        return "OK";
    }
}
