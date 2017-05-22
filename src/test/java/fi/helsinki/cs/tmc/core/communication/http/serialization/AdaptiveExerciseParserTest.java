/*
 * Author: Ohtu project summer devs 2017
 */

package fi.helsinki.cs.tmc.core.communication.http.serialization;

import static org.junit.Assert.assertEquals;

import fi.helsinki.cs.tmc.core.communication.serialization.AdaptiveExerciseParser;

import fi.helsinki.cs.tmc.core.domain.Exercise;

import org.junit.Before;
import org.junit.Test;


public class AdaptiveExerciseParserTest {

    private AdaptiveExerciseParser adaptiveParser;
    
    @Before
    public void setUp() {
        this.adaptiveParser = new AdaptiveExerciseParser();
    }

    @Test(expected = NullPointerException.class)
    public void jsonEmptyException() {
        adaptiveParser.parseFromJson(null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void jsonIllegalException() {
        adaptiveParser.parseFromJson(" ");
    }
    
    public void exerciseHasZipUrl() {
        Exercise exercise = adaptiveParser.parseFromJson("{ available: true, zip_url: additionToString }");
        assertEquals(exercise.getDownloadUrl().toString(), "http://localhost:3200additionToString");
    }
    
    @Test
    public void exerciseNotAvailable() {
        Exercise exercise = adaptiveParser.parseFromJson("{ available: false, zip_url: additionToString }");
        assertEquals(exercise, null);
    }
}
