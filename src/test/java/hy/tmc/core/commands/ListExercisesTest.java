package hy.tmc.core.commands;

import hy.tmc.core.communication.ExerciseLister;

import hy.tmc.core.testhelpers.ClientTmcSettings;
import hy.tmc.core.domain.Course;
import hy.tmc.core.exceptions.TmcCoreException;

import hy.tmc.core.domain.Exercise;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

public class ListExercisesTest {

    private ListExercises list;
    private ExerciseLister lister;
    private List<Exercise> exampleExercises;
    private ClientTmcSettings settings = new ClientTmcSettings();

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
        settings.setUsername("Chang");
        settings.setPassword("Jamo");
        lister = Mockito.mock(ExerciseLister.class);
        Mockito.when(lister.listExercises(Mockito.anyString()))
                .thenReturn(exampleExercises);

        list = new ListExercises(lister, settings);
    }

    @Test
    public void testCheckDataSuccess() throws TmcCoreException, IOException {
        settings.setCurrentCourse(new Course());
        ListExercises ls = new ListExercises(settings);
        ls.setParameter("courseUrl", "legit");
        ls.setParameter("path", "legit");
        ls.checkData();
    }

    @Test(expected = TmcCoreException.class)
    public void throwsErrorIfNoUser() throws TmcCoreException, IOException {
        settings.setCurrentCourse(new Course());
        settings.setUsername("");
        settings.setUsername("");
        list.setParameter("path", "any");
        list.checkData();
        list.call();
    }

    @Test(expected = TmcCoreException.class)
    public void throwsErrorIfNoCourseSpecified() throws TmcCoreException, IOException {
        list.checkData();
        list.call();
    }

    @Test
    public void listWithAuthSuccess() throws Exception {
        list.setParameter("path", "any");
        settings.setCurrentCourse(new Course());
        List<Exercise> exercises = list.call();
        assertEquals("1 tehtävä", exercises.get(1).getName());
    }
   
}
