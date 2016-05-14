package fi.helsinki.cs.tmc.core.utilities;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

// TODO: is this really to be used?
public class ExceptionUtils {
    public static String backtraceToString(Throwable throwable) {
        StringWriter sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    public static void logException(Throwable throwable, Logger log, Level level) {
        String msg = throwable.getMessage() + "\n" + backtraceToString(throwable);
        log.log(level, msg);
    }

    public static RuntimeException toRuntimeException(Exception ex) {
        if (ex instanceof RuntimeException) {
            return (RuntimeException) ex;
        } else {
            return new RuntimeException(ex);
        }
    }
}
