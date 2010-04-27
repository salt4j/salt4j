package salt4j.cache.evict;

import java.util.HashSet;
import salt4j.cache.Cache;

/** 
 * Stores a set of cache keys of type K and invalidates them all when evict() is called.
 */
public class Evictor <K> {
    final HashSet<K> set = new HashSet<K>();
    
    final Cache<K, ?> cache;
    public Evictor(Cache<K, ?> cache) { this.cache = cache; }

    public synchronized void register(K key) { set.add(key); }

    public void evict() {
        synchronized(cache) {
            synchronized(this) {
                for(K key: set) cache.evict(key);
                set.clear();
            }
        }
    }
}
