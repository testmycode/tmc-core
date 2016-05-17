package fi.helsinki.cs.tmc.core.persistance;

import java.io.IOException;

public interface TmcStatePersistor {

    void save(TmcState tmcState) throws IOException;

    TmcState load() throws IOException;
}
