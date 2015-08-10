package fi.helsinki.cs.tmc.core.communication.updates;

import fi.helsinki.cs.tmc.core.communication.TmcJsonParser;
import fi.helsinki.cs.tmc.core.domain.Course;

import java.util.ArrayList;
import java.util.List;

public abstract class UpdateHandler<T> {

    protected TmcJsonParser jsonParser;

    public UpdateHandler(TmcJsonParser jsonParser) {
        this.jsonParser = jsonParser;
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
