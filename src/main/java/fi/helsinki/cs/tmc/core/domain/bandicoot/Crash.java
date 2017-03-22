package fi.helsinki.cs.tmc.core.domain.bandicoot;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Crash implements Serializable {
    private final String name;
    private final String message;
    private final List<String> causes;
    private final List<String> stacktrace;
    @SerializedName("client_info_attributes")
    private final Diagnostics diagnostics;

    public Crash(Throwable throwable) {
        this.name = throwable.toString();
        this.message = throwable.getMessage();
        stacktrace = new ArrayList<>();
        this.diagnostics = new Diagnostics();

        this.causes = getCauses(throwable);

        for (StackTraceElement s : throwable.getStackTrace()) {
            stacktrace.add(s.toString());
        }
    }

    private static List<String> getCauses(final Throwable throwable) {
        List<String> res = new ArrayList<>();
        Set<Throwable> found = new HashSet<>();
        int depth = 0;
        Throwable cause = throwable.getCause();
        // Causes may be recursive
        while (cause != null && !found.contains(cause)) {
            if (depth > 20) {
                return res;
            }
            res.add(cause.toString());
            found.add(cause);
            cause = cause.getCause();
            depth++;
        }
        return res;
    }
}
