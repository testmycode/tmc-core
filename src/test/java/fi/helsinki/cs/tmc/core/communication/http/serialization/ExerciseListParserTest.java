package fi.helsinki.cs.tmc.core.communication.http.serialization;

import static org.junit.Assert.assertEquals;

import fi.helsinki.cs.tmc.core.communication.serialization.ExerciseListParser;
import fi.helsinki.cs.tmc.core.domain.Exercise;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class ExerciseListParserTest {
    
    private ExerciseListParser exerciseListParser;
    private List<Exercise> exerciseList;
    
    @Before
    public void setUp() {
        this.exerciseListParser = new ExerciseListParser();
        exerciseList = null;
    }

    @Test(expected = NullPointerException.class)
    public void jsonEmptyException() {
        exerciseListParser.parseFromJson(null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void jsonIllegalException() {
        exerciseListParser.parseFromJson(" ");
    }
    
    @Test
    public void singleExcerciseIsParsedAndSetToList() {
        String json = "[\n"
                + "    {\n"
                + "      \"id\": 1,\n"
                + "      \"name\": \"Exercise name\",\n"
                + "      \"disabled\": false,\n"
                + "      \"available_points\": [\n"
                + "        {\n"
                + "          \"id\": 1,\n"
                + "          \"exercise_id\": 1,\n"
                + "          \"name\": \"Point name\",\n"
                + "          \"require_review\": false\n"
                + "        }\n"
                + "      ]\n"
                + "    }\n"
                + "]";
        exerciseList = exerciseListParser.parseFromJson(json);
        assertEquals(1, exerciseList.size());
    }
    
    @Test
    public void setOfExcercisesIsParsedAndSetToList() {
        String json = "[\n"
                + "    {\n"
                + "      \"id\": 1,\n"
                + "      \"name\": \"Exercise name\",\n"
                + "      \"disabled\": false,\n"
                + "      \"available_points\": [\n"
                + "        {\n"
                + "          \"id\": 1,\n"
                + "          \"exercise_id\": 1,\n"
                + "          \"name\": \"Point name\",\n"
                + "          \"require_review\": false\n"
                + "        }\n"
                + "      ]\n"
                + "    },\n"
                + "    {\n"
                + "      \"id\": 2,\n"
                + "      \"name\": \"Exercise name2\",\n"
                + "      \"disabled\": false,\n"
                + "      \"available_points\": [\n"
                + "        {\n"
                + "          \"id\": 2,\n"
                + "          \"exercise_id\": 2,\n"
                + "          \"name\": \"Point name\",\n"
                + "          \"require_review\": false\n"
                + "        }\n"
                + "      ]\n"
                + "    }\n"
                + "]";
        exerciseList = exerciseListParser.parseFromJson(json);
        assertEquals(2, exerciseList.size());
    }
    
}
