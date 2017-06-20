package org.brainwy.liclipsetext.shared_core.lru_space_cache;

import java.util.Map;

/**
 * LRU cache which keeps strong references for values until a value exceeds the memory space in the LRU, in which case
 * the value is put in a map with soft values (so, in a get it may be gotten back and re-added to the LRU).
 *
 * @author Fabio Zadrozny
 */
public class LRUCacheWithSoftPrunedValues<K, V> extends LRUCache<K, V> {

    private final Map<K, V> auxiliaryCache;

    public LRUCacheWithSoftPrunedValues(int size) {
        this(size, new SoftHashMap<K, V>());
    }

    /**
     * This constructor is meant only to be used in unit-tests, as the map should always be a soft hash map (but may be
     * changed for testing purposes).
     */
    LRUCacheWithSoftPrunedValues(int size, Map<K, V> auxiliaryCache) {
        super(size);
        this.auxiliaryCache = auxiliaryCache;
    }

    @Override
    public V get(K key) {
        V value = super.get(key);
        if (value == null) {
            // Miss in LRU: check our auxiliary cache.
            V auxiliaryValue = auxiliaryCache.get(key);
            if (auxiliaryValue != null) {
                // Found in auxiliary cache: remove from there and put it back in the main cache.
                auxiliaryCache.remove(key);
                super.put(key, auxiliaryValue);
            }
            return auxiliaryValue;
        }
        return value;
    };

    @Override
    protected void privateRemoveEntry(LRUCacheEntry<K, V> entry, boolean shuffle) {
        super.privateRemoveEntry(entry, shuffle);
        if (!shuffle) {
            auxiliaryCache.put(entry.key, entry.value);
        }
    }

    @Override
    public boolean put(K key, V value) {
        boolean added = super.put(key, value);
        if (!added) {
            auxiliaryCache.put(key, value);
        } else {
            // If it was in the auxiliary cache, remove it from there
            auxiliaryCache.remove(key);
        }
        return added;
    };

    @Override
    public void flush() {
        super.flush();
        auxiliaryCache.clear();
    }
}