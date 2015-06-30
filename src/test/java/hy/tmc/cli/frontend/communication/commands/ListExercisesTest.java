package hy.tmc.cli.frontend.communication.commands;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import com.google.common.base.Optional;

import hy.tmc.cli.backend.Mailbox;
import hy.tmc.cli.backend.communication.ExerciseLister;

import hy.tmc.cli.configuration.ClientData;
import hy.tmc.cli.domain.Course;
import hy.tmc.cli.frontend.communication.server.ProtocolException;
import hy.tmc.cli.synchronization.TmcServiceScheduler;

import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import hy.tmc.cli.domain.Exercise;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.eq;

import org.mockito.Mockito;

import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ClientData.class)
public class ListExercisesTest {

    private ListExercises list;
    private ExerciseLister lister;
    private List<Exercise> exampleExercises;

    private void buildExample() {
        exampleExercises = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            Exercise ex = new Exercise();
            ex.setId(i);
            ex.setName(i + " tehtävä");
            ex.setAttempted(random.nextBoolean());
            ex.setCompleted(random.nextBoolean());

            exampleExercises.add(ex);
        }

    }

    @Before
    public void setup() throws ProtocolException, IOException {
        Mailbox.create();
        TmcServiceScheduler.disablePolling();
        buildExample();
        ClientData.setUserData("Chang", "Jamo");
        mock();
        lister = Mockito.mock(ExerciseLister.class);
        Mockito.when(lister.listExercises(Mockito.anyString()))
                .thenReturn(exampleExercises);

        list = new ListExercises(lister);
    }

    private void mock() throws ProtocolException, IOException {

        PowerMockito.mockStatic(ClientData.class);
        PowerMockito
                .when(ClientData.getCurrentCourse(Mockito.anyString()))
                .thenReturn(Optional.of(new Course()));
        PowerMockito
                .when(ClientData.getFormattedUserData())
                .thenReturn("Chang:Jamo");
        PowerMockito
                .when(ClientData.userDataExists())
                .thenReturn(true);
    }

    @Test
    public void testCheckDataSuccess() throws ProtocolException, IOException {
        ListExercises ls = new ListExercises();
        ls.setParameter("courseUrl", "legit");
        ls.setParameter("path", "legit");
        try {
            ls.checkData();
        } catch (ProtocolException p) {
            fail("testCheckDataSuccess failed");
        }
    }

    @Test
    public void getsExerciseName() throws Exception {
        list.setParameter("path", "any");
        when(lister.buildExercisesInfo(eq(exampleExercises))).thenCallRealMethod();
        String result = list.parseData(list.call()).get();
        assertTrue(result.contains("1 tehtävä"));
        assertTrue(result.contains("3 tehtävä"));
    }

    @Test
    public void returnsFailIfBadPath() throws ProtocolException, Exception {
        String found = "No course found";
        Mockito.when(lister.buildExercisesInfo(eq(exampleExercises))).thenReturn(found);
        list.setParameter("path", "any");
        String result = list.parseData(list.call()).get();
        assertTrue(result.contains(found));

    }

    @Test(expected = ProtocolException.class)
    public void throwsErrorIfNoUser() throws ProtocolException, IOException {
        PowerMockito.mockStatic(ClientData.class);
        ClientData.clearUserData();
        list.setParameter("path", "any");
        list.checkData();
        list.call();
    }

    @Test(expected = ProtocolException.class)
    public void throwsErrorIfNoCourseSpecified() throws ProtocolException, IOException {
        ClientData.clearUserData();
        list.checkData();
        list.call();
    }

    @Test
    public void doesntContainWeirdName() throws ProtocolException, IOException {
        list.setParameter("path", "any");
        when(lister.buildExercisesInfo(eq(exampleExercises))).thenCallRealMethod();

        String result = list.parseData(list.call()).get();
        assertFalse(result.contains("Ilari"));
    }
}
