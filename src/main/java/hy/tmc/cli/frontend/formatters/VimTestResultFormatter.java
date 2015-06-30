package hy.tmc.cli.frontend.formatters;

import fi.helsinki.cs.tmc.langs.RunResult;
import fi.helsinki.cs.tmc.langs.TestResult;
import java.util.List;

public class VimTestResultFormatter implements TestResultFormatter {

    private String testPadding;
    private String stackTracePadding;

    /**
     * VimTestResultFormatter gives testresult explainings for vim user. ResultInterpreter class
     * uses this class.
     */
    public VimTestResultFormatter() {
        this.testPadding = "  ";
        this.stackTracePadding = testPadding + " ";
    }

    /**
     * Interprets status of RunResult.
     *
     * @param result RunResult
     */
    @Override
    public String interpretStatus(RunResult result) {
        switch (result.status) {
            case PASSED:
                return "All tests passed. You can now submit";
            case COMPILE_FAILED:
                return "Code did not compile.";
            case GENERIC_ERROR:
                return "Failed due to an internal error";
            default:
                throw new IllegalArgumentException("bad argument");
        }
    }

    /**
     * Some tests failed -explanation
     */
    @Override
    public String someTestsFailed() {
        return "Some tests failed:\n";
    }

    /**
     * No tests passed -explanation
     */
    @Override
    public String noTestsPassed() {
        return "No tests passed.\n";
    }

    /**
     * How much tests passed.
     */
    @Override
    public String howMuchTestsPassed(int amount) {
        String passed = amount + " tests passed:\n";
        return passed;
    }

    /**
     * Get how much tests failed.
     */
    @Override
    public String howMuchTestsFailed(int amount) {
        String failed = amount + " tests failed:\n";
        return failed;
    }

    /**
     * Get explanation of passed tests
     */
    @Override
    public String getPassedTests(List<TestResult> passed) {
        StringBuilder passedList = new StringBuilder();
        for (TestResult testResult : passed) {
            passedList.append(testPadding).append(testResult.name).append("\n");
        }
        return passedList.toString();
    }

    /**
     * Get explanation of a single failed testResult. Expects that result is failed.
     */
    @Override
    public String getFailedTestOutput(TestResult failed) {
        StringBuilder output = new StringBuilder();
        output.append(testPadding);
        output.append(failed.name)
                .append(" failed: ")
                .append(failed.errorMessage)
                .append("\n");
        return output.toString();
    }

    /**
     * Get stack trace of test result.
     */
    @Override
    public String getStackTrace(TestResult result) {
        StringBuilder builder = new StringBuilder();
        for (String line : result.backtrace) {
            builder.append(stackTracePadding).append(line).append("\n");
        }
        return builder.toString();
    }

}
