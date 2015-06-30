package hy.tmc.core.commands;

import com.google.common.base.Optional;
import hy.tmc.core.exceptions.ProtocolException;

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
    public String call() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
