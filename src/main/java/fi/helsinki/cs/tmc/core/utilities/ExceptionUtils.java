package fi.helsinki.cs.tmc.core.utilities;

import java.io.PrintWriter;
import java.io.StringWriter;

// TODO: is this really to be used?
public class ExceptionUtils {
    public static String backtraceToString(Throwable throwable) {
        StringWriter sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    public static RuntimeException toRuntimeException(Exception ex) {
        if (ex instanceof RuntimeException) {
            return (RuntimeException) ex;
        } else {
            return new RuntimeException(ex);
        }
    }
}
