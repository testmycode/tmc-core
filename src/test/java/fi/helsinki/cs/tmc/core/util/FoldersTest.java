package fi.helsinki.cs.tmc.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@RunWith(PowerMockRunner.class)
public class FoldersTest {

    @Test
    public void testTempFolder() {
        assertTrue(Files.isDirectory(Folders.tempFolder()));
    }

    @Test
    public void testTempDoesNotChange() {
        assertEquals(Folders.tempFolder(), Folders.tempFolder());
    }

    @Test
    @PrepareForTest(Folders.class)
    public void testTempfolderFallsBack() throws IOException {
        PowerMockito.mockStatic(Files.class);
        Mockito.when(Files.createTempDirectory(anyString())).thenThrow(new IOException());
        assertEquals(Folders.tempFolder(), Paths.get(System.getProperty("java.io.tmpdir")));
    }
}
