package hy.tmc.cli.frontend.communication.commands;

import com.google.common.base.Optional;
import hy.tmc.cli.frontend.communication.server.ProtocolException;

public class Feedback extends Command<String> {

    protected Optional<String> functionality() {
        return null;
    }

    @Override
    public void checkData() throws ProtocolException {
        if (!data.containsKey("question")) {
            throw new ProtocolException("Question missing");
        }
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
