/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.helsinki.cs.tmc.core.communication.http.serialization;

import fi.helsinki.cs.tmc.core.communication.serialization.AdaptiveExerciseParser;
import fi.helsinki.cs.tmc.core.domain.Exercise;

import org.junit.Before;
import org.junit.Test;

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

    @Test
    public void jsonEmptyException() {
        assertEquals(adaptiveParser.parseFromJson(null), new NullPointerException("Json string is null"));
    }
    
    @Test
    public void jsonIllegalException() {
        assertEquals(adaptiveParser.parseFromJson(" "), new IllegalArgumentException("Empty input"));
    }
    
    @Test
    public void exerciseHasZipUrl() {
        Exercise exercise = adaptiveParser.parseFromJson("{ zip_url: not-empty }");
        assertEquals(exercise.getZipUrl(), "not-empty");
    }
}
