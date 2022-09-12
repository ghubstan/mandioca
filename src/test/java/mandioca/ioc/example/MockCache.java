package mandioca.ioc.example;

import mandioca.ioc.annotation.Singleton;

import java.util.Map;
import java.util.TreeMap;

import static java.lang.System.identityHashCode;

public class MockCache {

    private final Map<String, Object> cache = new TreeMap<>();

    @Singleton
    public MockCache() {
    }

    public void addToCache(String key, Object value) {
        cache.put(key, value);  // Can't use cache.put( new AbstractMap.SimpleEntry<>(key, value) )
    }

    public boolean containsKey(String key) {
        return cache.containsKey(key);
    }

    public Integer numCacheEntries() {
        return cache.size();
    }

    @Override
    public String toString() {
        return "MockCache{" +
                "id=" + identityHashCode(this) +
                ", cache=" + cache +
                ", cache-id=" + identityHashCode(cache) +
                '}';
    }
}