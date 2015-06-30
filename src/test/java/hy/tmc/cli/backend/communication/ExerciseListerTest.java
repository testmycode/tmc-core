package hy.tmc.cli.backend.communication;

import com.google.common.base.Optional;
import hy.tmc.cli.configuration.ClientData;
import hy.tmc.cli.domain.Course;
import hy.tmc.cli.domain.Exercise;
import hy.tmc.core.exceptions.ProtocolException;
import hy.tmc.cli.zipping.ProjectRootFinder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(TmcJsonParser.class)
public class ExerciseListerTest {

    String fakeName = "2014-mooc-no-deadline";
    String otherFakeName = "2013-tira";
    ProjectRootFinder rootFinderMock;
    Course fakeCourse;
    ExerciseLister lister;
    Exercise fakeExercise;
    Exercise fakeExercise2;

    @Before
    public void setUp() throws IOException, ProtocolException {
        ClientData.setUserData("chang", "paras");
        setupFakeCourses();

        rootFinderMock = Mockito.mock(ProjectRootFinder.class);
        Mockito.when(rootFinderMock.getCurrentCourse(Mockito.anyString()))
                .thenReturn(Optional.of(fakeCourse));
        lister = new ExerciseLister(rootFinderMock);

        PowerMockito.mockStatic(TmcJsonParser.class);

        mockExercisesWith(setupFakeExercises());

    }

    private void mockExercisesWith(List<Exercise> exercises) throws IOException, ProtocolException {
        PowerMockito
                .when(TmcJsonParser.getExercises((Course) Mockito.any()))
                .thenReturn(exercises);
    }

    private List<Exercise> setupFakeExercises() {
        List<Exercise> exercises = new ArrayList<>();

        fakeExercise = new Exercise();
        fakeExercise.setName("Nimi");

        fakeExercise2 = new Exercise();
        fakeExercise2.setName("Kuusi");
        fakeExercise2.setCompleted(true);

        exercises.add(fakeExercise);
        exercises.add(fakeExercise2);
        return exercises;
    }

    private void setupFakeCourses() {
        fakeCourse = new Course();
        fakeCourse.setName(fakeName);
        fakeCourse.setId(99);
    }

    @After
    public void tearDown() {
        ClientData.clearUserData();
    }

    @Test(expected=ProtocolException.class)
    public void ifNoCourseIsFoundThenThrowsProtocolException() throws ProtocolException, IOException {
        Mockito.when(rootFinderMock.getCurrentCourse(Mockito.anyString()))
                .thenReturn(Optional.<Course>absent());
        lister.listExercises("polku/tuntemattomaan/tiedostoon");
    }

    @Test(expected=ProtocolException.class)
    public void ifNoExercisesFoundThenThrowsProtocolException() throws ProtocolException, IOException {
        String correct = "No exercises found";

        mockExercisesWith(null);

        assertEquals(correct, lister.listExercises("any"));
    }
   
}
