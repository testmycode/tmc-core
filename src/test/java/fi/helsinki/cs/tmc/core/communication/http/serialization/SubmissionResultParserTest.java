package fi.helsinki.cs.tmc.core.communication.http.serialization;

import static com.google.common.truth.Truth.assertThat;

import fi.helsinki.cs.tmc.core.communication.serialization.SubmissionResultParser;
import fi.helsinki.cs.tmc.core.domain.submission.SubmissionResult;
import fi.helsinki.cs.tmc.core.utils.TestUtils;
import fi.helsinki.cs.tmc.langs.abstraction.Strategy;
import fi.helsinki.cs.tmc.langs.abstraction.ValidationError;
import fi.helsinki.cs.tmc.langs.abstraction.ValidationResult;
import fi.helsinki.cs.tmc.langs.domain.TestResult;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

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
        for (String str : test.getException()) {
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
        assertThat(test.getDetailedMessage()).isInstanceOf(ImmutableList.class);
        assertThat(test.getDetailedMessage().size()).isEqualTo(26);
        for (String str : test.getDetailedMessage()) {
            assertThat(str).isInstanceOf(String.class);
        }
    }

    @Test
    public void parsesTestResultsWithFailedCheckstyle() throws IOException {
        String json = TestUtils.readJsonFile(this.getClass(), "checkstyleFailed.json");
        SubmissionResult submissionResult = parser.parseFromJson(json);
        assertThat(submissionResult).isNotNull();

        ValidationResult result = submissionResult.getValidationResult();
        assertThat(result.getStrategy()).isEqualTo(Strategy.FAIL);

        Map<File, List<ValidationError>> filesErrors = result.getValidationErrors();
        assertThat(filesErrors).hasSize(1);

        List<ValidationError> errors = filesErrors.get(new File("error/errorMessages.java"));
        assertThat(errors).hasSize(73);

        for (ValidationError error : errors) {
            assertThat(error).isNotNull();
            assertThat(error.getMessage()).isNotNull();
            assertThat(error.getSourceName()).isNotNull();
        }
    }
}
