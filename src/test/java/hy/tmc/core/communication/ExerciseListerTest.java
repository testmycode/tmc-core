package hy.tmc.core.communication;

import com.google.common.base.Optional;
import hy.tmc.core.configuration.ClientTmcSettings;
import hy.tmc.core.domain.Course;
import hy.tmc.core.domain.Exercise;
import hy.tmc.core.exceptions.TmcCoreException;
import hy.tmc.core.zipping.ProjectRootFinder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import static org.junit.Assert.assertEquals;
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
    private ClientTmcSettings settings;

    @Before
    public void setUp() throws IOException, TmcCoreException {
        settings = new ClientTmcSettings();
        settings.setUsername("chang");
        settings.setPassword("rajani");
        setupFakeCourses();

        rootFinderMock = Mockito.mock(ProjectRootFinder.class);
        Mockito.when(rootFinderMock.getCurrentCourse(Mockito.anyString()))
                .thenReturn(Optional.of(fakeCourse));
        lister = new ExerciseLister(rootFinderMock);

        PowerMockito.mockStatic(TmcJsonParser.class);

        mockExercisesWith(setupFakeExercises());

    }

    private void mockExercisesWith(List<Exercise> exercises) throws IOException, TmcCoreException {
        PowerMockito
                .when(TmcJsonParser.getExercisesFromServer((Course) Mockito.any()))
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
    
    
    @Test(expected=TmcCoreException.class)
    public void ifNoCourseIsFoundThenThrowsProtocolException() throws TmcCoreException, IOException {
        Mockito.when(rootFinderMock.getCurrentCourse(Mockito.anyString()))
                .thenReturn(Optional.<Course>absent());
        lister.listExercises("polku/tuntemattomaan/tiedostoon");
    }

    @Test(expected=TmcCoreException.class)
    public void ifNoExercisesFoundThenThrowsProtocolException() throws TmcCoreException, IOException {
        String correct = "No exercises found";

        mockExercisesWith(null);

        assertEquals(correct, lister.listExercises("any"));
    }
   
}
