package hy.tmc.core.commands;

import static org.junit.Assert.fail;

import com.google.common.base.Optional;

import hy.tmc.core.communication.ExerciseLister;

import hy.tmc.core.configuration.ClientTmcSettings;
import hy.tmc.core.domain.Course;
import hy.tmc.core.exceptions.TmcCoreException;

import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import hy.tmc.core.domain.Exercise;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import static org.junit.Assert.assertEquals;


import org.junit.Before;
import org.junit.Test;


import org.mockito.Mockito;


@RunWith(PowerMockRunner.class)
@PrepareForTest(ClientTmcSettings.class)
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
    public void setup() throws TmcCoreException, IOException {
        buildExample();
        ClientTmcSettings.setUserData("Chang", "Jamo");
        mock();
        lister = Mockito.mock(ExerciseLister.class);
        Mockito.when(lister.listExercises(Mockito.anyString()))
                .thenReturn(exampleExercises);

        list = new ListExercises(lister);
    }

    private void mock() throws TmcCoreException, IOException {

        PowerMockito.mockStatic(ClientTmcSettings.class);
        PowerMockito
                .when(ClientTmcSettings.getCurrentCourse(Mockito.anyString()))
                .thenReturn(Optional.of(new Course()));
        PowerMockito
                .when(ClientTmcSettings.getFormattedUserData())
                .thenReturn("Chang:Jamo");
        PowerMockito
                .when(ClientTmcSettings.userDataExists())
                .thenReturn(true);
    }

    @Test
    public void testCheckDataSuccess() throws TmcCoreException, IOException {
        ListExercises ls = new ListExercises();
        ls.setParameter("courseUrl", "legit");
        ls.setParameter("path", "legit");
        try {
            ls.checkData();
        } catch (TmcCoreException p) {
            fail("testCheckDataSuccess failed");
        }
    }

    @Test(expected = TmcCoreException.class)
    public void throwsErrorIfNoUser() throws TmcCoreException, IOException {
        PowerMockito.mockStatic(ClientTmcSettings.class);
        ClientTmcSettings.clearUserData();
        list.setParameter("path", "any");
        list.checkData();
        list.call();
    }

    @Test(expected = TmcCoreException.class)
    public void throwsErrorIfNoCourseSpecified() throws TmcCoreException, IOException {
        ClientTmcSettings.clearUserData();
        list.checkData();
        list.call();
    }
    
    @Test
    public void listWithAuthSuccess() throws Exception {
        list.setParameter("path", "any");
        List<Exercise> exercises = list.call();
        assertEquals("1 tehtävä", exercises.get(1).getName());
    }
    
}
