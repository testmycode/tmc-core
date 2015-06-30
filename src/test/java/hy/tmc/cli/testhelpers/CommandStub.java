package hy.tmc.cli.testhelpers;

import com.google.common.base.Optional;
import hy.tmc.cli.frontend.communication.commands.Command;
import hy.tmc.cli.frontend.communication.server.ProtocolException;

//To change return value of this command, change class definition
public class CommandStub extends Command<String> {

    protected Optional<String> functionality() {     
        return Optional.absent();
    }

    @Override
    public void checkData() throws ProtocolException {        
    }    

    @Override
    @SuppressWarnings("unchecked")
    public Optional<String> parseData(Object data) {
        String data1 = (String) data;
        Optional<String> of = Optional.of(data1);
        return of;
    }

    @Override
    public String call() throws Exception {
        return "OK";
    }
}
