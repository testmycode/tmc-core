package fi.helsinki.cs.tmc.core.communication.http.serialization;

import static com.google.common.truth.Truth.assertThat;

import fi.helsinki.cs.tmc.core.communication.serialization.SubmissionResultParser;
import fi.helsinki.cs.tmc.core.domain.submission.SubmissionResult;
import fi.helsinki.cs.tmc.core.utils.TestUtils;
import fi.helsinki.cs.tmc.langs.domain.TestResult;
import fi.helsinki.cs.tmc.testrunner.TestCase;

import com.google.common.collect.ImmutableList;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.URI;
import java.util.List;

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
        List<TestResult> tests = result.getTestCases();
        assertThat(tests.size()).isEqualTo(1);
        TestResult test = tests.get(0);
        assertThat(test.getException()).isInstanceOf(ImmutableList.class);
        assertThat(test.getException().size()).isEqualTo(27);
        for (String str : test.getException()){
            assertThat(str).isInstanceOf(String.class);
        }
    }


    @Test
    public void parsesLangsTestResult() throws Exception {
        String json = TestUtils.readJsonFile(this.getClass(), "staging_failed_hello_world.json");
        SubmissionResult result = parser.parseFromJson(json);
        List<TestResult> tests = result.getTestCases();
        assertThat(tests.size()).isEqualTo(1);
        TestResult test = tests.get(0);
        assertThat(test.getException()).isInstanceOf(ImmutableList.class);
        assertThat(test.getException().size()).isEqualTo(27);
        for (String str : test.getException()){
            assertThat(str).isInstanceOf(String.class);
        }
    }
}
