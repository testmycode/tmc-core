package hy.tmc.core.testhelpers.testresults;

import static org.junit.Assert.fail;

import fi.helsinki.cs.tmc.langs.domain.TestResult;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TestResultFactory {

    /**
     * Create some failed tests, based on tests from a real tmc server.
     *
     * @return a list of testcases that have failed.
     */
    public static List<TestResult> failedTests() {
        List<TestResult> tests = new ArrayList<>();
        tests.add(kuusi());
        tests.add(toinenKuusiTesti());

        return tests;
    }

    private static TestResult kuusi() {
        TestResultBuilder builder = new TestResultBuilder();

        builder.withName("KuusiTest test")
                .withPassedStatus(false)
                .withErrorMessage("Ohjelmasi pitäisi tulostaa 6 riviä, eli siinä pitäisi olla 6"
                        + " System.out.println()-komentoa. expected:<6> but was:<1>")
                .withStackTrace(stackTrace());
        return builder.build();
    }

    private static TestResult toinenKuusiTesti() {
        TestResultBuilder builder = new TestResultBuilder();

        builder.withName("KuusiTest test")
                .withErrorMessage("ComparisonFailure: Kuusen toinen rivi on väärin expected:"
                        + "<  [ *]**> but was:<  []**>")
                .withPassedStatus(false)
                .withStackTrace(stackTrace());
        return builder.build();
    }

    private static List<String> stackTrace() {
        List<String> trace = new ArrayList<>();
        try {
            Scanner scanner = new Scanner(new File("src/test/resources/stacktrace.txt"));
            while (scanner.hasNextLine()) {
                trace.add(scanner.nextLine());
            }
        } catch (FileNotFoundException ex) {
            fail("unable to read test sources, namely stacktrace.txt");
        }
        return trace;
    }

    /**
     * Create some passed tests, based on tests from a real tmc server.
     *
     * @return a list of testcases that have passed.
     */
    public static List<TestResult> passedTests() {
        List<TestResult> tests = new ArrayList<>();
        TestResultBuilder builder = new TestResultBuilder();
        tests.add(builder.withName("Muuttujat testaaKanat").withPassedStatus(true).build());
        tests.add(builder.withName("Muuttujat testaaPekoni").withPassedStatus(true).build());
        tests.add(builder.withName("Muuttujat testaaTraktori").withPassedStatus(true).build());
        return tests;
    }

}
