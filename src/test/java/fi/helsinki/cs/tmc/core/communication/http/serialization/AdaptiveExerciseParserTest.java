package fi.helsinki.cs.tmc.core.communication.http.serialization;

import static org.junit.Assert.assertEquals;

import fi.helsinki.cs.tmc.core.communication.serialization.AdaptiveExerciseParser;
import fi.helsinki.cs.tmc.core.domain.Exercise;

import org.junit.Before;
import org.junit.Test;

import java.net.URI;

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

    @Test
    public void exerciseNotAvailable() {
        Exercise exercise = adaptiveParser.parseFromJson("{ available: false, zip_url: additionToString }");
        assertEquals(exercise, null);
    }

    @Test
    public void exersiceAvailableTest() {
        Exercise exercise = adaptiveParser.parseFromJson("{available: true, zip_url: additionToString }");
        assertEquals(URI.create("http://additionToString"), exercise.getZipUrl());
    }
}
