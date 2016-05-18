package fi.helsinki.cs.tmc.core.holders;

import static org.junit.Assert.assertEquals;

import fi.helsinki.cs.tmc.core.exceptions.UninitializedHolderException;
import fi.helsinki.cs.tmc.langs.util.TaskExecutor;
import fi.helsinki.cs.tmc.langs.util.TaskExecutorImpl;

import org.junit.Test;

public class TmcLangsHolderTest {

    private TmcLangsHolder holder;

    @Test(expected = UninitializedHolderException.class)
    public void throwsIfNotInitialized() {
        holder.set(null);
        holder.get();
    }

    @Test
    public void returnsCorrectAfterSet() {
        TaskExecutor langs = new TaskExecutorImpl();
        holder.set(langs);
        assertEquals(langs, holder.get());
    }
}
