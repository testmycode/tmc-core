package hy.tmc.core.commands;

import hy.tmc.core.commands.Paste;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import com.google.common.base.Optional;

import hy.tmc.core.communication.CourseSubmitter;
import hy.tmc.core.configuration.ClientData;
import hy.tmc.core.domain.Course;
import hy.tmc.core.exceptions.ExpiredException;
import hy.tmc.core.exceptions.TmcCoreException;
import hy.tmc.core.testhelpers.ProjectRootFinderStub;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import static org.junit.Assert.assertEquals;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ClientData.class)
public class PasteTest {

    private Paste paste;
    private CourseSubmitter submitterMock;
    private String pasteUrl = "http://legit.paste.url.fi";

    /**
     * Mocks CourseSubmitter and injects it into Paste command.
     */
    @Before
    public void setup() throws Exception {
        mock();
        ClientData.setUserData("Bossman", "Samu");
        ClientData.setProjectRootFinder(new ProjectRootFinderStub());
        submitterMock = Mockito.mock(CourseSubmitter.class);
        when(submitterMock.submitPaste(Mockito.anyString())).thenReturn(pasteUrl);
        paste = new Paste(submitterMock);
    }
    
    private void mock() throws ParseException, ExpiredException, IOException, TmcCoreException {
        ClientData.setUserData("Massbon", "Samu");
        PowerMockito.mockStatic(ClientData.class);
        PowerMockito
                .when(ClientData.getCurrentCourse(Mockito.anyString()))
                .thenReturn(Optional.<Course>of(new Course()));
        PowerMockito
                .when(ClientData.getFormattedUserData())
                .thenReturn("Bossman:Samu");
    }

    @After
    public void clean() {
        ClientData.clearUserData();
    }

    /**
     * Check that data checking success.
     */
    @Test
    public void testCheckDataSuccess() throws TmcCoreException, IOException {
        PowerMockito.when(ClientData.userDataExists()).thenReturn(true);
        paste.setParameter("path", "/home/tmccli/uolevipuistossa");
        paste.checkData();
    }
    
    @Test
    public void pasteSuccess() throws Exception {
        PowerMockito.when(ClientData.userDataExists()).thenReturn(true);
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
        ClientData.clearUserData();
        paste.checkData();
    }

    @Test(expected = TmcCoreException.class)
    public void throwsErrorIfNoCredentialsPresent() throws Exception {
        paste.data.put("path", "asdsad");
        ClientData.clearUserData();
        paste.checkData();
    }
    
    @Test(expected = TmcCoreException.class)
    public void throwsErrorIfNoPath() throws Exception {
        PowerMockito.when(ClientData.userDataExists()).thenReturn(true);
        paste.checkData();
    }

    @Test(expected = TmcCoreException.class)
    public void throwsErrorIfCourseCantBeRetrieved() throws Exception {
        PowerMockito.when(ClientData.userDataExists()).thenReturn(true);
        PowerMockito
                .when(ClientData.getCurrentCourse(Mockito.anyString()))
                .thenReturn(Optional.<Course>absent());
        paste.data.put("path", "asdsad");
        paste.checkData();
    }
}
