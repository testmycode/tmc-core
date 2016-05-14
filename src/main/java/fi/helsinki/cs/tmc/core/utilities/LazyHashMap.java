package fi.helsinki.cs.tmc.core.utilities;

import java.util.HashMap;
import java.util.concurrent.Callable;

/**
 * A hash map that creates values on demand in {@link #get(java.lang.Object)}.
 */
public class LazyHashMap<K, V> extends HashMap<K, V> {
    private Callable<V> factory;

    public LazyHashMap(Callable<V> factory) {
        this.factory = factory;
    }

    @Override
    @SuppressWarnings("unchecked")
    public V get(Object key) {
        V value = super.get(key);
        if (value == null) {
            try {
                value = factory.call();
            } catch (Exception ex) {
                throw ExceptionUtils.toRuntimeException(ex);
            }
            put((K) key, value);
        }
        return value;
    }
}
