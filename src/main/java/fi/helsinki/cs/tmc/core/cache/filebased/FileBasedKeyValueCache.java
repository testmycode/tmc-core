package fi.helsinki.cs.tmc.core.cache.filebased;

import fi.helsinki.cs.tmc.core.cache.KeyValueCache;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

public class FileBasedKeyValueCache<K, V> extends FileBasedCache<ConcurrentMap<K, V>> implements KeyValueCache<K, V> {

    public FileBasedKeyValueCache(Path cacheFile) throws FileNotFoundException {
        super(cacheFile);
    }

    @Override
    public void put(K key, V value) throws IOException {
        ConcurrentMap<K, V> cache = read();
        cache.put(key, value);
        write(cache);
    }

    @Override
    public void clearValue(K key) throws IOException {
        ConcurrentMap<K, V> cache = read();
        cache.put(key, null);
        write(cache);
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


}
