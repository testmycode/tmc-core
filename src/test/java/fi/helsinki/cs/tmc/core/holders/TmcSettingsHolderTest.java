package fi.helsinki.cs.tmc.core.holders;

import static org.junit.Assert.assertEquals;

import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.exceptions.UninitializedHolderException;
import fi.helsinki.cs.tmc.core.utils.MockSettings;

import org.junit.Before;
import org.junit.Test;

import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

public class TmcSettingsHolderTest {

    private TmcSettingsHolder holder;

    @Spy private TmcSettings settings = new MockSettings();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test(expected = UninitializedHolderException.class)
    public void throwsIfNotInitialized() {
        holder.set(null);
        holder.get();
    }

    @Test
    public void returnsCorrectAfterSet() {
        holder.set(settings);
        assertEquals(settings, holder.get());
    }
}
