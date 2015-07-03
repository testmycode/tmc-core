package hy.tmc.core.communication.updates;

import hy.tmc.core.communication.TmcJsonParser;
import hy.tmc.core.configuration.TmcSettings;
import hy.tmc.core.domain.Course;
import java.util.ArrayList;
import java.util.List;

public abstract class UpdateHandler<T> {

    protected TmcJsonParser jsonParser;
    
    public UpdateHandler(TmcSettings settings) {
        jsonParser = new TmcJsonParser(settings);
    }
    
    protected abstract boolean isNew(T object);

    public abstract List<T> fetchFromServer(Course course) throws Exception;

    public List<T> getNewObjects(Course course) throws Exception {
        List<T> objects = filterNew(fetchFromServer(course));

        return objects;
    }

    private List<T> filterNew(List<T> objects) {
        List<T> newObjects = new ArrayList<>();
        for (T object : objects) {
            if (isNew(object)) {
                newObjects.add(object);
            }
        }
        return newObjects;
    }
}
