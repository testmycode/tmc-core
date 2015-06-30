package hy.tmc.cli.frontend.communication.commands;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import com.google.common.base.Optional;

import hy.tmc.cli.backend.Mailbox;
import hy.tmc.cli.backend.communication.CourseSubmitter;
import hy.tmc.cli.configuration.ClientData;
import hy.tmc.cli.domain.Course;
import hy.tmc.cli.frontend.communication.server.ExpiredException;
import hy.tmc.cli.frontend.communication.server.ProtocolException;
import hy.tmc.cli.synchronization.TmcServiceScheduler;
import hy.tmc.cli.testhelpers.ProjectRootFinderStub;
import net.lingala.zip4j.exception.ZipException;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.text.ParseException;

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
        Mailbox.create();
        mock();
        ClientData.setUserData("Bossman", "Samu");
        ClientData.setProjectRootFinder(new ProjectRootFinderStub());
        TmcServiceScheduler.disablePolling();
        submitterMock = Mockito.mock(CourseSubmitter.class);
        when(submitterMock.submitPaste(Mockito.anyString())).thenReturn(pasteUrl);
        paste = new Paste(submitterMock);

    }

    private void mock() throws ParseException, ExpiredException, IOException, ProtocolException {
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

    @Test
    public void submitReturnsBadOutputWhenCodeIsBad() throws Exception {
        PowerMockito.when(ClientData.userDataExists()).thenReturn(true);
        paste.setParameter("path", "/hieno/path");
        String result = paste.parseData(paste.call()).get();
        assertTrue(result.contains(pasteUrl));
    }

    /**
     * Check that data checking success.
     */
    @Test
    public void testCheckDataSuccess() throws ProtocolException, IOException {
        PowerMockito.when(ClientData.userDataExists()).thenReturn(true);
        paste.setParameter("path", "/home/tmccli/uolevipuistossa");
        paste.checkData();
    }

    /**
     * Check that if user didn't give correct data, data checking fails.
     */
    @Test(expected = ProtocolException.class)
    public void testCheckDataFail() throws Exception {
        paste.checkData();
    }

    @Test(expected = ProtocolException.class)
    public void checkDataFailIfNoAuth() throws Exception {
        ClientData.clearUserData();
        paste.checkData();
    }

    @Test(expected = ProtocolException.class)
    public void throwsErrorIfNoCredentialsPresent() throws Exception {
        paste.data.put("path", "asdsad");
        ClientData.clearUserData();
        paste.checkData();
    }

    @Test(expected = ProtocolException.class)
    public void throwsErrorIfCourseCantBeRetrieved() throws Exception {
        PowerMockito.when(ClientData.userDataExists()).thenReturn(true);
        PowerMockito
                .when(ClientData.getCurrentCourse(Mockito.anyString()))
                .thenReturn(Optional.<Course>absent());
        paste.data.put("path", "asdsad");
        paste.checkData();
    }
}
