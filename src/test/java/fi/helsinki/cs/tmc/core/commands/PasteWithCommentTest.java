package fi.helsinki.cs.tmc.core.commands;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import fi.helsinki.cs.tmc.core.CoreTestSettings;
import fi.helsinki.cs.tmc.core.communication.ExerciseSubmitter;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.exceptions.ExpiredException;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;

import com.google.common.base.Optional;

import net.lingala.zip4j.exception.ZipException;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import java.io.IOException;
import java.net.URI;
import java.text.ParseException;

public class PasteWithCommentTest {
    private PasteWithComment paste;
    private ExerciseSubmitter submitterMock;
    private String pasteUrl = "http://example.com/paste";
    private CoreTestSettings settings = new CoreTestSettings();

    @Before
    public void setup() throws Exception {
        mock();
        submitterMock = Mockito.mock(ExerciseSubmitter.class);
        when(submitterMock.submitPasteWithComment(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(pasteUrl);
    }

    private void mock() {
        settings = Mockito.mock(CoreTestSettings.class);
        Mockito.when(settings.getUsername()).thenReturn("Samu");
        Mockito.when(settings.getPassword()).thenReturn("Bossman");
        Mockito.when(settings.getCurrentCourse()).thenReturn(Optional.of(new Course()));
        Mockito.when(settings.getFormattedUserData()).thenReturn("Bossman:Samu");
    }

    @Test
    public void testCheckDataSuccess()
            throws TmcCoreException, IOException, ParseException, ExpiredException, ZipException {
        Mockito.when(settings.userDataExists()).thenReturn(true);

        new PasteWithComment(settings, "path", "comment", submitterMock).call();
    }

    @Test
    public void pasteSuccess() throws Exception {
        Mockito.when(settings.userDataExists()).thenReturn(true);

        URI uri = new PasteWithComment(settings, "path", "comment", submitterMock).call();
        assertEquals(uri.toString(), "http://example.com/paste");
    }

    @Test(expected = TmcCoreException.class)
    public void testThrowsExceptionWithoutPath() throws Exception {
        new PasteWithComment(settings, null, "comment", submitterMock).call();
    }

    @Test(expected = TmcCoreException.class)
    public void testThrowsExceptionIfAuthFails() throws Exception {
        settings = new CoreTestSettings();
        new PasteWithComment(settings, "path", "comment", submitterMock).call();
    }

    @Test(expected = TmcCoreException.class)
    public void throwsErrorIfCourseCantBeRetrieved() throws Exception {
        Mockito.when(settings.userDataExists()).thenReturn(true);
        Mockito.when(settings.getCurrentCourse()).thenReturn(Optional.<Course>absent());
        new PasteWithComment(settings, "path", "comment", submitterMock).call();
    }
}
