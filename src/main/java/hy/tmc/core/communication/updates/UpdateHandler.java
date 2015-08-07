package hy.tmc.core.communication.updates;

import hy.tmc.core.communication.TmcJsonParser;
import hy.tmc.core.domain.Course;

import java.util.ArrayList;
import java.util.List;

public abstract class UpdateHandler<T> {

    protected TmcJsonParser jsonParser;

    public UpdateHandler(TmcJsonParser jsonParser) {
        this.jsonParser = jsonParser;
    }

    public abstract List<T> fetchFromServer(Course course) throws Exception;

    protected abstract boolean isNew(T object);

    public List<T> getNewObjects(Course course) throws Exception {
        return filterNew(fetchFromServer(course));
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
