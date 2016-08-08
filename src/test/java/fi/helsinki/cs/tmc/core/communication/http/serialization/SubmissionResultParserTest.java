package fi.helsinki.cs.tmc.core.communication.http.serialization;

import fi.helsinki.cs.tmc.core.communication.serialization.SubmissionResultParser;
import fi.helsinki.cs.tmc.core.domain.submission.SubmissionResult;
import fi.helsinki.cs.tmc.core.utils.TestUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.URI;

public class SubmissionResultParserTest {


    private SubmissionResultParser parser;

    @Before
    public void setUp() {
        this.parser = new SubmissionResultParser();
    }

    @Test
    public void parsesNonLangsTestResult() throws Exception {
        String json = TestUtils.readJsonFile(this.getClass(), "mooc_failed_hello_world.json");
        SubmissionResult result = parser.parseFromJson(json);
    }

    @Test
    public void parsesLangsTestResult() throws Exception {
        String json = TestUtils.readJsonFile(this.getClass(), "staging_failed_hello_world.json");
        SubmissionResult result = parser.parseFromJson(json);
    }
}
