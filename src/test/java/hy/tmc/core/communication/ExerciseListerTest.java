package hy.tmc.core.communication;

import com.google.common.base.Optional;
import hy.tmc.core.ClientTmcSettings;
import hy.tmc.core.domain.Course;
import hy.tmc.core.domain.Exercise;
import hy.tmc.core.exceptions.TmcCoreException;
import hy.tmc.core.zipping.ProjectRootFinder;
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
import static org.mockito.Matchers.any;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import static org.powermock.api.mockito.PowerMockito.mock;
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
    private TmcJsonParser jsonParser;

    @Before
    public void setUp() throws IOException, TmcCoreException {
        settings = new ClientTmcSettings();
        settings.setUsername("chang");
        settings.setPassword("rajani");
        setupFakeCourses();
        
        jsonParser = mock(TmcJsonParser.class);

        rootFinderMock = Mockito.mock(ProjectRootFinder.class);
        Mockito.when(rootFinderMock.getCurrentCourse(Mockito.anyString()))
                .thenReturn(Optional.of(fakeCourse));
        lister = new ExerciseLister(rootFinderMock, jsonParser);

        PowerMockito.mockStatic(TmcJsonParser.class);

        mockExercisesWith(setupFakeExercises());

    }

    private void mockExercisesWith(List<Exercise> exercises) throws IOException, TmcCoreException {
        Mockito.when(jsonParser.getExercisesFromServer(any(Course.class)))
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
    
    @Test
    public void correctCoursesAreOnList() throws TmcCoreException, IOException {
        List<Exercise> exercises = lister.listExercises("polku/tiedostoon/");
        
        assertTrue(exerciseWithNameOnList(exercises, "Nimi"));
        assertTrue(exerciseWithNameOnList(exercises, "Kuusi"));
    }
    
    @Test
    public void otherCoursesAreNotOnList() throws TmcCoreException, IOException {
        List<Exercise> exercises = lister.listExercises("polku/tiedostoon/");
        
        assertFalse(exerciseWithNameOnList(exercises, "asdf"));
        assertFalse(exerciseWithNameOnList(exercises, "Ankka"));
    }
    
    private boolean exerciseWithNameOnList(List<Exercise> exercises, String name) {
        for (Exercise exercise : exercises) {
            if (exercise.getName().equals(name)) {
                return true;
            }
        }
        return false;
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
