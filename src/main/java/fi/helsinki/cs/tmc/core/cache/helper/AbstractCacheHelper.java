package fi.helsinki.cs.tmc.core.cache.helper;

import fi.helsinki.cs.tmc.core.cache.Cache;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;

import com.google.common.base.Optional;

public abstract class AbstractCacheHelper<K, V> {

    private final Cache.QueryStrategy queryStrategy;

    public AbstractCacheHelper(Cache.QueryStrategy queryStrategy) {
        this.queryStrategy = queryStrategy;
    }

    public V get(K key) throws TmcCoreException {
        Optional<V> value;

        if (queryStrategy == Cache.QueryStrategy.FORCE_UPDATE) {
            value = getFromServer(key);
            if (value.isPresent()) {
                cache(key, value.get());
                return value.get();
            } else {
                throw new TmcCoreException("Attempted to fetch non-existent course from server");
            }
        }

        if (queryStrategy == Cache.QueryStrategy.PREFER_LOCAL) {
            value = getFromCache(key);
            if (value.isPresent()) {
                return value.get();
            }

            value = getFromServer(key);
            if (value.isPresent()) {
                cache(key, value.get());
                return value.get();
            }

            throw new TmcCoreException("Failed to fetch course details from both cache and server");
        }

        value = getFromServer(key);
        if (value.isPresent()) {
            cache(key, value.get());
            return value.get();
        }

        value = getFromCache(key);
        if (value.isPresent()) {
            return value.get();
        }

        throw new TmcCoreException("Failed to fetch course details from both cache and server");
    }

    protected abstract Optional<V> getFromServer(K key) throws TmcCoreException;

    protected abstract Optional<V> getFromCache(K key) throws TmcCoreException;

    protected abstract void cache(K key, V value) throws TmcCoreException;
}
