package hy.tmc.core.testhelpers;

import com.google.common.base.Optional;

import hy.tmc.core.commands.Command;
import hy.tmc.core.exceptions.TmcCoreException;

//To change return value of this command, change class definition
public class CommandStub extends Command<String> {

    protected Optional<String> functionality() {
        return Optional.absent();
    }

    @Override
    public void checkData() throws TmcCoreException {}

    @Override
    public String call() throws Exception {
        return "OK";
    }
}
