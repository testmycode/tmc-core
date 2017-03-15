package fi.helsinki.cs.tmc.core.domain.bandicoot;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Crash implements Serializable {
    private final String name;
    private final String message;
    private final String cause;
    private final List<String> stacktrace;
    @SerializedName("client_info_attributes")
    private final Diagnostics diagnostics;

    public Crash(Throwable throwable) {
        this.name = throwable.toString();
        this.message = throwable.getMessage();
        stacktrace = new ArrayList<>();
        this.diagnostics = new Diagnostics();
        Optional<Throwable> cause = Optional.fromNullable(throwable.getCause());

        if (cause.isPresent()) {
            this.cause = cause.get().getMessage();
        } else {
            this.cause = null;
        }

        for (StackTraceElement s : throwable.getStackTrace()) {
            stacktrace.add(s.toString());
        }
    }
}
