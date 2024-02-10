package com.s54488630.CPEN431.A4;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.*;

// This class is partially ChatGPT-generated

public class ExpiringLRUCache<K, V> {
    private final long expiryTimeInMillis;
    private final Map<K, ValueWithTimestamp<V>> cacheMap;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public ExpiringLRUCache(int capacity, long expiryTimeInMillis) {
        this.expiryTimeInMillis = expiryTimeInMillis;
        this.cacheMap = new LinkedHashMap<K, ValueWithTimestamp<V>>() {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, ValueWithTimestamp<V>> eldest) {
                return size() > capacity ||
                        System.currentTimeMillis() - eldest.getValue().timestamp > expiryTimeInMillis;
            }
        };

        // Periodically clean up the cache
        scheduler.scheduleAtFixedRate(this::removeExpiredEntries, expiryTimeInMillis, expiryTimeInMillis, TimeUnit.MILLISECONDS);
    }

    public synchronized void put(K key, V value) {
        cacheMap.put(key, new ValueWithTimestamp<>(value));
    }

    public synchronized V get(K key) {
        ValueWithTimestamp<V> valueWithTimestamp = cacheMap.get(key);
        if (valueWithTimestamp == null) {
            return null;
        }
        // Update timestamp on access
        valueWithTimestamp.timestamp = System.currentTimeMillis();
        return valueWithTimestamp.value;
    }

    public synchronized boolean containsKey(K key) {
        return cacheMap.containsKey(key);
    }

    private synchronized void removeExpiredEntries() {
        long now = System.currentTimeMillis();
        cacheMap.entrySet().removeIf(entry -> now - entry.getValue().timestamp > this.expiryTimeInMillis);
    }

    public void clear() {
        this.cacheMap.clear();
    }

    private static class ValueWithTimestamp<V> {
        long timestamp;
        V value;

        ValueWithTimestamp(V value) {
            this.value = value;
            this.timestamp = System.currentTimeMillis();
        }
    }


    public void shutdown() {
        this.cacheMap.clear();
        scheduler.shutdown();
    }


}
