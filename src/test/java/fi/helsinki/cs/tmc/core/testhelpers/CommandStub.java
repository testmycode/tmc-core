package fi.helsinki.cs.tmc.core.testhelpers;

import fi.helsinki.cs.tmc.core.commands.Command;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;

import com.google.common.base.Optional;

//To change return value of this command, change class definition
public class CommandStub extends Command<String> {

    protected Optional<String> functionality() {
        return Optional.absent();
    }

    @Override
    public String call() throws Exception {
        return "OK";
    }
}
