
package hy.tmc.cli.frontend.formatters;

import fi.helsinki.cs.tmc.langs.RunResult;
import static fi.helsinki.cs.tmc.langs.RunResult.Status.COMPILE_FAILED;
import static fi.helsinki.cs.tmc.langs.RunResult.Status.GENERIC_ERROR;
import static fi.helsinki.cs.tmc.langs.RunResult.Status.PASSED;
import static fi.helsinki.cs.tmc.langs.RunResult.Status.TESTS_FAILED;
import fi.helsinki.cs.tmc.langs.TestResult;
import hy.tmc.cli.testhelpers.testresults.RunResultBuilder;
import hy.tmc.cli.testhelpers.testresults.TestResultFactory;
import java.util.List;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;


public class VimTestResultFormatterTest {
    private RunResult allPassed;
    private RunResult allFailed;
    private RunResult someFailed;
    private RunResult compileError;
    private RunResult genericError;
    private RunResultBuilder builder;
    private VimTestResultFormatter formatter;
    private List<TestResult> passed;
    
    public VimTestResultFormatterTest(){
        allPassed = new RunResultBuilder().withStatus(PASSED).build();
        compileError = new RunResultBuilder().withStatus(COMPILE_FAILED).build();
        genericError = new RunResultBuilder().withStatus(GENERIC_ERROR).build();
        allFailed = new RunResultBuilder()
                .withStatus(TESTS_FAILED)
                .withTests(TestResultFactory.failedTests())
                .build();
        formatter = new VimTestResultFormatter();
        passed = TestResultFactory.passedTests();
    }
    
    @Test
    public void testInterpretStatus() {
        String explanation = formatter.interpretStatus(allPassed);
        assertTrue(explanation.contains("All tests passed"));
    }
    
    /**
     * \u001B[32m is success color code
     */
    @Test
    public void ensureThatThereAreNoColorsWhenAllArePassed() {
        String explanation = formatter.interpretStatus(allPassed);
        assertFalse(explanation.contains("\u001B[32m"));
    }

    @Test
    public void interpretGenericError() {
        String explanation = formatter.interpretStatus(genericError);
        assertTrue(explanation.contains("Failed due to an internal error"));
    }
    
    @Test
    public void getFailedTestsOutput(){
        String explanation = formatter.getFailedTestOutput(allFailed.testResults.get(0));
        assertTrue(explanation.contains("KuusiTest test failed: Ohjelmasi pitäisi tulostaa"));
    }

    @Test
    public void someTestsFailed() {
        String explanation = formatter.someTestsFailed();
        assertTrue(explanation.contains("Some tests failed:\n"));
    }

    @Test
    public void noTestsPassed() {
        String explanation = formatter.noTestsPassed();
        assertTrue(explanation.contains("No tests passed.\n"));
    }

    @Test
    public void testHowMuchTestsPassed() {
        String explanation = formatter.howMuchTestsPassed(5);
        assertTrue(explanation.contains("5 tests passed:\n"));
    }

    @Test
    public void testHowMuchTestsFailed() {
        String explanation = formatter.howMuchTestsFailed(5);
        assertTrue(explanation.contains("5 tests failed:\n"));
    }
    
    /**
     * \u001B[31m is fail color code
     */
    @Test
    public void ensureThatThereAreNoColorsWhenSomeFail() {
        String explanation = formatter.howMuchTestsFailed(5);
        assertFalse(explanation.contains("\u001B[31m"));
    }

    @Test
    public void testPassedTests() {
        String explanation = formatter.getPassedTests(passed);
        String shouldBe = "  Muuttujat testaaKanat";
        assertTrue(explanation.contains(shouldBe));
    }
    
    @Test
    public void testgetStackTrace(){
        String explanation = formatter.getStackTrace(allFailed.testResults.get(0));
        assertTrue(explanation.contains("org.junit.ComparisonFailure: Kuusen toinen rivi on väärin expected:< [ *]**"));
    }

    @Test
    public void compileFailedMessageIsRight() {
        String interpretStatus = formatter.interpretStatus(compileError);
        assertTrue(interpretStatus.contains("Code did not compile."));
    }

}
