package mandioca.bitcoin.network.node;

import mandioca.ioc.annotation.Singleton;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.identityHashCode;

public final class HandshakeCache {

    private Map<String, Long> map = new TreeMap<>();
    private final Map<String, Long> cache = Collections.synchronizedMap(map);

    @Singleton
    public HandshakeCache() {
    }

    public void add(String address /*host:port*/, Long expiry) {
        cache.put(address, expiry);
    }

    public void remove(String address /*host:port*/) {
        cache.remove(address);
    }

    public boolean isCached(String address) {
        return !isExpired(address);
    }

    public boolean isExpired(String address) {
        Long expiry = cache.get(address);
        if (expiry == null) {
            return true;
        } else if (currentTimeMillis() > expiry) {
            remove(address);
            return true;
        } else {
            return false;
        }
    }

    public Integer size() {
        return cache.size();
    }

    public int removeExpired() {
        long now = currentTimeMillis();
        int originalSize = size();
        synchronized (cache) {
            cache.entrySet().removeIf(entry -> now >= entry.getValue());
        }
        return originalSize - size();
    }

    public void clear() {
        cache.clear();
    }

    @Override
    public String toString() {
        return "HandshakeCache{" +
                "id=" + identityHashCode(this) +
                ", cache=" + cache +
                ", cache-id=" + identityHashCode(cache) +
                '}';
    }

}
