package hy.tmc.core.testhelpers;

import hy.tmc.core.commands.Command;
import hy.tmc.core.exceptions.TmcCoreException;

import com.google.common.base.Optional;

public class BadCommandStub extends Command<String> {

    protected Optional<String> functionality() {
        return null;
    }

    @Override
    public void checkData() throws TmcCoreException {
        throw new TmcCoreException("I'm a bad command-stub :(");
    }

    @Override
    public String call() throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
