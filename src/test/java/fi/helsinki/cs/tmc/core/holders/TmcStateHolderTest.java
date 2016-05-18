package fi.helsinki.cs.tmc.core.holders;

import static org.junit.Assert.assertEquals;

import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.exceptions.UninitializedHolderException;
import fi.helsinki.cs.tmc.core.persistance.TmcState;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class TmcStateHolderTest {

    private TmcStateHolder holder;
    private TmcState tmcState;

    @Test(expected = UninitializedHolderException.class)
    public void throwsIfNotInitialized() {
        holder.set(null);
        holder.get();
    }

    @Test
    public void returnsCorrectAfterSet() {
        tmcState = new TmcState();
        holder.set(tmcState);
        assertEquals(tmcState, holder.get());
    }
}
