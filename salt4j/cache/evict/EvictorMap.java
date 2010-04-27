package salt4j.cache.evict;

import java.util.HashMap;
import salt4j.cache.Cache;

/**
 *  _Asociates_ each unique id with a set of cache keys and
 *  evicts all entries in the associated set when evict(id) is called
 */
public class EvictorMap<ID, K> {
    private final Cache<K, ?> cache;
    public EvictorMap(Cache<K, ?> cache) { this.cache = cache; }

    private final HashMap<ID, Evictor<K>> map = new HashMap<ID, Evictor<K>>();

    public synchronized void register(ID id, K key) {
        Evictor<K> evictor = map.get(id);
        if (evictor == null) {
            evictor = new Evictor<K>(cache);
            map.put(id, evictor);
        }
        evictor.register(key);
    }

    public void evict(ID id) {
        synchronized(cache) { //always lock cache first.
            synchronized(this) {
                Evictor<K> evictor = map.get(id);
                if (evictor != null) evictor.evict();
            }
        }
    }
}
