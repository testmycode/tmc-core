package fi.helsinki.cs.tmc.core.cache.inmemory;

import fi.helsinki.cs.tmc.core.cache.KeyValueCache;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InMemoryKeyValueCache<K, V> extends InMemoryCache<ConcurrentMap<K, V>> implements KeyValueCache<K, V> {

    public InMemoryKeyValueCache() {
        super(new ConcurrentHashMap<K, V>());
    }

    @Override
    public void put(K key, V value) throws IOException {
        read().put(key, value);
    }

    @Override
    public void clearValue(K key) throws IOException {
        read().put(key, null);
    }

    @Override
    public V get(K key) throws IOException {
        return read().get(key);
    }

    @Override
    public Set<K> keySet() throws IOException {
        return read().keySet();
    }

    @Override
    public Collection<V> values() throws IOException {
        return read().values();
    }

    @Override
    public void clear() throws IOException {
        read().clear();
    }
}
