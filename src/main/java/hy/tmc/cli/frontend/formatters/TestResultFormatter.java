
package hy.tmc.cli.frontend.formatters;

import fi.helsinki.cs.tmc.langs.RunResult;
import fi.helsinki.cs.tmc.langs.TestResult;

import java.util.List;

/**
 * TestResultFormatter interface makes easier to attach new frontends to core. 
 * TestResult is formatted with some class which implements this interface.
 */
public interface TestResultFormatter {
    public String interpretStatus(RunResult result);
    
    public String someTestsFailed();
    
    public String noTestsPassed();
    
    public String howMuchTestsPassed(int amount);
    
    public String howMuchTestsFailed(int amount);
    
    public String getPassedTests(List<TestResult> passed);
    
    public String getFailedTestOutput(TestResult failed);
    
    public String getStackTrace(TestResult result);
}
