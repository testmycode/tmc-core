
package hy.tmc.core.commands;

import com.google.common.base.Optional;
import hy.tmc.core.communication.ExerciseSubmitter;
import hy.tmc.core.domain.Course;
import hy.tmc.core.exceptions.ExpiredException;
import hy.tmc.core.exceptions.TmcCoreException;
import hy.tmc.core.testhelpers.ClientTmcSettings;
import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;


public class PasteWithCommentTest {
    private PasteWithComment paste;
    private ExerciseSubmitter submitterMock;
    private String pasteUrl = "http://legit.paste.url.fi";
    ClientTmcSettings settings = new ClientTmcSettings();

    /**
     * Mocks CourseSubmitter and injects it into Paste command.
     */
    @Before
    public void setup() throws Exception {
        mock();
        //ClientTmcSettings.setProjectRootFinder(new ProjectRootFinderStub());
        submitterMock = Mockito.mock(ExerciseSubmitter.class);
        when(submitterMock.submitPasteWithComment(Mockito.anyString(), Mockito.anyString())).thenReturn(pasteUrl);
        paste = new PasteWithComment(submitterMock, settings, "Commentti");
    }
    
    private void mock() throws ParseException, ExpiredException, IOException, TmcCoreException {
        settings = Mockito.mock(ClientTmcSettings.class);
        Mockito.when(settings.getUsername()).thenReturn("Samu");
        Mockito.when(settings.getPassword()).thenReturn("Bossman");
        Mockito.when(settings.getCurrentCourse()).thenReturn(Optional.of(new Course()));
        Mockito
                .when(settings.getFormattedUserData())
                .thenReturn("Bossman:Samu");
    }

    /**
     * Check that data checking success.
     */
    @Test
    public void testCheckDataSuccess() throws TmcCoreException, IOException {
        Mockito.when(settings.userDataExists()).thenReturn(true);
        paste.setParameter("path", "/home/tmccli/uolevipuistossa");
        paste.checkData();
    }
    
    @Test
    public void pasteSuccess() throws Exception {
        Mockito.when(settings.userDataExists()).thenReturn(true);
        paste.data.put("path", "asdsad");
        URI uri = paste.call();
        assertEquals(uri.toString(), "http://legit.paste.url.fi");
    }

    /**
     * Check that if user didn't give correct data, data checking fails.
     */
    @Test(expected = TmcCoreException.class)
    public void testCheckDataFail() throws Exception {
        paste.checkData();
    }

    @Test(expected = TmcCoreException.class)
    public void checkDataFailIfNoAuth() throws Exception {
        settings = new ClientTmcSettings();
        paste.checkData();
    }

    @Test(expected = TmcCoreException.class)
    public void throwsErrorIfNoCredentialsPresent() throws Exception {
        paste.data.put("path", "asdsad");
        settings = new ClientTmcSettings();
        paste.checkData();
    }
    
    @Test(expected = TmcCoreException.class)
    public void throwsErrorIfNoPath() throws Exception {
        Mockito.when(settings.userDataExists()).thenReturn(true);
        paste.checkData();
    }

    @Test(expected = TmcCoreException.class)
    public void throwsErrorIfCourseCantBeRetrieved() throws Exception {
        Mockito.when(settings.userDataExists()).thenReturn(true);
        Mockito.when(settings.getCurrentCourse()).thenReturn(Optional.<Course>absent());
        paste.data.put("path", "asdsad");
        paste.checkData();
    }
}
