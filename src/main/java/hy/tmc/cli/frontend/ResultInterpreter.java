package hy.tmc.cli.frontend;


import static fi.helsinki.cs.tmc.langs.RunResult.Status.TESTS_FAILED;

import fi.helsinki.cs.tmc.langs.RunResult;
import fi.helsinki.cs.tmc.langs.TestResult;

import hy.tmc.cli.frontend.formatters.TestResultFormatter;

import java.util.ArrayList;
import java.util.List;

public class ResultInterpreter {

    private final RunResult result;
    private final TestResultFormatter formatter;

    public ResultInterpreter(RunResult result, TestResultFormatter formatter) {
        this.result = result;
        this.formatter = formatter;
    }

    /**
     * Transform the RunResult given to this interpreter in its constructor into a human readable
     * output.
     *
     * @param showStackTrace decide whether to include stacktrace in output
     * @return a String representation of a RunResult
     */
    public String interpret(boolean showStackTrace) {
        if (result.status == TESTS_FAILED) {
            return testFailureReport(showStackTrace);
        } else {
            return formatter.interpretStatus(result);
        }
    }

    private String testFailureReport(boolean showStackTrace) {
        StringBuilder reportBuilder = new StringBuilder();
        reportBuilder.append(formatter.someTestsFailed());

        succesfulTests(reportBuilder);
        failedTests(reportBuilder, showStackTrace);

        return reportBuilder.toString();
    }

    private void succesfulTests(StringBuilder builder) {
        List<TestResult> passedTests = getPassedTests();
        if (passedTests.isEmpty()) {
            builder.append(formatter.noTestsPassed());
            return;
        }
        builder.append(formatter.howMuchTestsPassed(passedTests.size()));
        builder.append(formatter.getPassedTests(passedTests));
    }

    private void failedTests(StringBuilder builder, boolean showStackTrace) {
        List<TestResult> failures = getFailedTests();
        builder.append(formatter.howMuchTestsFailed(failures.size()));

        for (TestResult testResult : failures) {
            failedTestOutput(builder, testResult, showStackTrace);
        }
    }

    private void failedTestOutput(StringBuilder builder,
                                  TestResult testResult, boolean showStackTrace) {
        builder.append(formatter.getFailedTestOutput(testResult));
        if (showStackTrace) {
            builder.append(formatter.getStackTrace(testResult));
        }
    }

    private List<TestResult> getPassedTests() {
        List<TestResult> passes = new ArrayList<>();
        for (TestResult test : result.testResults) {
            if (test.passed) {
                passes.add(test);
            }
        }
        return passes;
    }

    private List<TestResult> getFailedTests() {
        List<TestResult> fails = new ArrayList<>();
        for (TestResult test : result.testResults) {
            if (!test.passed) {
                fails.add(test);
            }
        }
        return fails;
    }
}
