package hy.tmc.cli.frontend.communication.commands;

import com.google.common.base.Optional;
import fi.helsinki.cs.tmc.langs.NoLanguagePluginFoundException;
import fi.helsinki.cs.tmc.langs.RunResult;
import fi.helsinki.cs.tmc.langs.util.TaskExecutorImpl;
import hy.tmc.cli.frontend.ResultInterpreter;
import hy.tmc.cli.frontend.communication.server.ProtocolException;
import hy.tmc.cli.frontend.formatters.CommandLineTestResultFormatter;
import hy.tmc.cli.frontend.formatters.TestResultFormatter;
import hy.tmc.cli.frontend.formatters.VimTestResultFormatter;
import hy.tmc.cli.zipping.DefaultRootDetector;
import hy.tmc.cli.zipping.ProjectRootFinder;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RunTests extends Command<RunResult> {

    private TestResultFormatter formatter;

    public RunTests(String path) {
        this.setParameter("path", path);
    }
    
    public RunTests(){
        
    }

    private TestResultFormatter getFormatter() {
        if (data.containsKey("--vim")) {
            return new VimTestResultFormatter();
        } else {
            return new CommandLineTestResultFormatter();
        }
    }

    /**
     * Runs tests for exercise.
     *
     * @param exercise Path object
     * @return String contaning results
     * @throws NoLanguagePluginFoundException if path doesn't contain exercise
     */
    public RunResult runTests(Path exercise) throws NoLanguagePluginFoundException {
        TaskExecutorImpl taskExecutor = new TaskExecutorImpl();
        return taskExecutor.runTests(exercise);

    }

    @Override
    public void checkData() throws ProtocolException {
        if (!this.data.containsKey("path") || this.data.get("path").isEmpty()) {
            throw new ProtocolException("File path to exercise required.");
        }
    }

    @Override
    public Optional<String> parseData(Object data) {
        RunResult result = (RunResult) data;
        boolean showStackTrace = this.data.containsKey("verbose");
        ResultInterpreter resInt = new ResultInterpreter(result, formatter);
        return Optional.of(resInt.interpret(showStackTrace));

    }

    @Override
    public RunResult call() throws ProtocolException, NoLanguagePluginFoundException {
        formatter = getFormatter();
        String path = (String) this.data.get("path");
        ProjectRootFinder finder = new ProjectRootFinder(new DefaultRootDetector());
        Optional<Path> exercise = finder.getRootDirectory(Paths.get(path));
        if (!exercise.isPresent()) {
            throw new ProtocolException("Not an exercise. (null)");
        }
        return runTests(exercise.get());
    }
}
