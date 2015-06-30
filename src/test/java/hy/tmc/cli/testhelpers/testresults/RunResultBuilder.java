package hy.tmc.cli.testhelpers.testresults;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import fi.helsinki.cs.tmc.langs.RunResult;
import fi.helsinki.cs.tmc.langs.RunResult.Status;
import fi.helsinki.cs.tmc.langs.TestResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class RunResultBuilder {

    Status status;
    ArrayList<TestResult> tests;
    HashMap<String, byte[]> logs;
    
    public RunResultBuilder() {
        this.tests = new ArrayList<>();
        this.logs = new HashMap<>();
    }
    
    public RunResultBuilder withStatus(Status status) {
        this.status = status;
        return this;
    }
    
    public RunResultBuilder withTest(TestResult result) {
        this.tests.add(result);
        return this;
    }
    
    public RunResultBuilder withTests(List<TestResult> results) {
        this.tests.addAll(results);
        return this;
    }
    
    public RunResultBuilder withLog(String name, byte[] bytes) {
        this.logs.put(name, bytes);
        return this;
    }
    
    
    public RunResult build() {
        return new RunResult(status, ImmutableList.copyOf(tests), ImmutableMap.copyOf(logs));
    }
}
