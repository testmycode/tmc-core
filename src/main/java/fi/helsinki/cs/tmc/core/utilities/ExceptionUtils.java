package fi.helsinki.cs.tmc.core.utilities;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

// TODO: is this really to be used?
public class ExceptionUtils {
    public static String backtraceToString(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
    
    public static void logException(Throwable t, Logger log, Level level) {
        String msg = t.getMessage() + "\n" + backtraceToString(t);
        log.log(level, msg);
    }

    public static RuntimeException toRuntimeException(Exception ex) {
        if (ex instanceof RuntimeException) {
            return (RuntimeException)ex;
        } else {
            return new RuntimeException(ex);
        }
    }
}
