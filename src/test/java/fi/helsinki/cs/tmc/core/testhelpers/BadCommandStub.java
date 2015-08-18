package fi.helsinki.cs.tmc.core.testhelpers;

import fi.helsinki.cs.tmc.core.commands.Command;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;

import com.google.common.base.Optional;

public class BadCommandStub extends Command<String> {

    protected Optional<String> functionality() {
        return null;
    }

    @Override
    public String call() throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
