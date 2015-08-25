package fi.helsinki.cs.tmc.core.cache;

import java.io.IOException;

public interface Cache<T> {

    public enum QueryStrategy {
        PREFER_LOCAL,
        PREFER_SERVER,
        FORCE_UPDATE
    }

    void write(T data) throws IOException;

    T read() throws IOException;

    void clear() throws IOException;
}
