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

import java.net.URI;

import static org.junit.Assert.*;

/**
 * @author sakuolin
 */
public class AdaptiveExerciseParserTest {

    private AdaptiveExerciseParser aep;

    @Before
    public void setUp() {
        this.aep = new AdaptiveExerciseParser();
    }

    @Test
    public void adaptiveParserReturnsNullWhenNotAvailable() {
        assertEquals(aep.parseFromJson("{ available: false }"), null);
    }

    @Test
    public void adaptiveParserReturnsExerciseWhenAvailable() {
        Exercise ex = aep.parseFromJson("{ available: true, zip_url: \"/path/to/zip\" }");
        assertEquals(URI.create("/path/to/zip"), ex.getZipUrl());
    }

}
