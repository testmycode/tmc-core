package fi.helsinki.cs.tmc.core.persistance;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

public class OnDiskTmcStatePersistor implements TmcStatePersistor {

    private Path location;

    public OnDiskTmcStatePersistor(Path location) {
        this.location = location;
    }

    @Override
    public void save(TmcState tmcState) throws IOException {
        Gson gson = new Gson();
        byte[] json = gson.toJson(tmcState).getBytes();
        Files.write(location, json);
    }

    @Override
    public TmcState load() throws IOException {
        Gson gson = new Gson();
        Reader reader = Files.newBufferedReader(location, Charset.forName("UTF-8"));
        return gson.fromJson(reader, TmcState.class);
    }
}
