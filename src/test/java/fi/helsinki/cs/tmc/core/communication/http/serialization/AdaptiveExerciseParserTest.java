/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.helsinki.cs.tmc.core.communication.http.serialization;

import fi.helsinki.cs.tmc.core.communication.serialization.AdaptiveExerciseParser;
import fi.helsinki.cs.tmc.core.domain.Exercise;

import fi.helsinki.cs.tmc.core.domain.Exercise;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.*;

/**
 * @author sakuolin
 */
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
        Exercise exercise = adaptiveParser.parseFromJson("{ available: true, zip_url: not-empty }");
        assertEquals(exercise.getZipUrl(), "not-empty");
    }
    
    @Test
    public void exerciseNotAvailable() {
        Exercise exercise = adaptiveParser.parseFromJson("{ available: false, zip_url: not-empty }");
        assertEquals(exercise.getZipUrl(), "not-empty");
    }
}
