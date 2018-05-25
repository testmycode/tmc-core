package fi.helsinki.cs.tmc.snapshots;

import fi.helsinki.cs.tmc.core.persistance.ConfigFileIo;
import fi.helsinki.cs.tmc.core.utilities.ByteArrayGsonSerializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class EventStore {
    private static final Logger log = LoggerFactory.getLogger(EventStore.class.getName());

    private ConfigFileIo configFile;

    // TODO: where the config file goes?
    public EventStore() {
        this.configFile = new ConfigFileIo("Events.json");
    }

    public void save(LoggableEvent[] events) throws IOException {
        String text = getGson().toJson(events);
        configFile.writeContents(text);
        log.info("Saved {0} events", events.length);
    }

    public LoggableEvent[] load() throws IOException {
        String text = configFile.readContents();
        LoggableEvent[] result = getGson().fromJson(text, LoggableEvent[].class);
        if (result == null) {
            result = new LoggableEvent[0];
        }
        log.info("Loaded {0} events", result.length);
        return result;
    }

    private Gson getGson() {
        return new GsonBuilder()
                .registerTypeAdapter(byte[].class, new ByteArrayGsonSerializer())
                .create();
    }

    public void clear() throws IOException {
        configFile.writeContents("");
    }
}
