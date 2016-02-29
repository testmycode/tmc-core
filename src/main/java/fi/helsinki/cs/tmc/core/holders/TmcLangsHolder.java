package fi.helsinki.cs.tmc.core.holders;

import fi.helsinki.cs.tmc.core.exceptions.UninitializedHolderException;
import fi.helsinki.cs.tmc.langs.util.TaskExecutor;

public class TmcLangsHolder {

    private static TaskExecutor taskExecutor;

    private TmcLangsHolder() {}

    public static synchronized TaskExecutor get() {
        if (taskExecutor == null) {
            throw new UninitializedHolderException();
        }
        return taskExecutor;
    }

    public static synchronized void set(TaskExecutor taskExecutor) {
        TmcLangsHolder.taskExecutor = taskExecutor;
    }
}
