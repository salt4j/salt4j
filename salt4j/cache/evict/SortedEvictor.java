package salt4j.cache.evict;

import java.util.NavigableMap;
import java.util.TreeMap;
import salt4j.cache.Cache;

/**
 * Maintains a Set<K> of keys, ordered by Comparable ORD associated with each key.
 * Allows you invalidate the keys whose associated ORD is >, >=, <, or <= any given value.
 */
public class SortedEvictor<ORD extends Comparable, K> {
    private final Cache<K, ?> cache;
    public SortedEvictor(Cache<K, ?> cache) { this.cache = cache; }

    private final TreeMap<ORD, K> map = new TreeMap<ORD, K>();

    public synchronized void register(ORD comparable, K key) {
        map.put(comparable, key);
    }

    public void evictAbove(ORD comparable, boolean orEqual) {
        synchronized(cache) {
            synchronized(this) {
                NavigableMap<ORD, K> subMap = map.tailMap(comparable, orEqual);
                for(K key: subMap.values()) cache.evict(key);
                subMap.clear();
            }
        }
    }

     public void evictBelow(ORD comparable, boolean orEqual) {
        synchronized(cache) {
            synchronized(this) {
                 NavigableMap<ORD, K> subMap = map.headMap(comparable, orEqual);
                 for(K key: subMap.values()) cache.evict(key);
                 subMap.clear();
            }
        }
     }

     public void evictBetween(ORD from, boolean fromInclusive, ORD to, boolean toInclusive) {
        synchronized(cache) {
            synchronized(this) {
                 NavigableMap<ORD, K> subMap = map.subMap(from, fromInclusive, to, toInclusive);
                 for(K key: subMap.values()) cache.evict(key);
                 subMap.clear();
            }
        }
     }
}
