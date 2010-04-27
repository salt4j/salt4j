package salt4j.cache.evict;

import java.util.HashMap;
import salt4j.cache.Cache;

/** Associates each ID with a SortedEvictor */
public class SortedEvictorMap<ID, ORD extends Comparable, K> {
    private final Cache<K, ?> cache;
    public SortedEvictorMap(Cache<K, ?> cache) { this.cache = cache; }

    private final HashMap<ID, SortedEvictor<ORD, K>> map = new HashMap<ID, SortedEvictor<ORD, K>>();

    public synchronized void register(ID id, ORD comparable, K key) {
        SortedEvictor<ORD, K> evictor = map.get(id);
        if (evictor == null) {
            evictor = new SortedEvictor<ORD, K>(cache);
            map.put(id, evictor);
        }
        evictor.register(comparable, key);
    }

    public void evictAbove(ID id, ORD comparable, boolean inclusive) {
        synchronized(cache) { //always lock cache first.
            synchronized(this) {
                SortedEvictor<ORD, K> evictor = map.get(id);
                if (evictor != null) evictor.evictAbove(comparable, inclusive);
            }
        }
    }

    public void evictBelow(ID id, ORD comparable, boolean inclusive) {
        synchronized(cache) { //always lock cache first.
            synchronized(this) {
                SortedEvictor<ORD, K> evictor = map.get(id);
                if (evictor != null) evictor.evictBelow(comparable, inclusive);
            }
        }
    }

    public void evictBetween(ID id, ORD from, boolean fromInclusive, ORD to, boolean toInclusive) {
          synchronized(cache) { //always lock cache first.
            synchronized(this) {
                SortedEvictor<ORD, K> evictor = map.get(id);
                if (evictor != null) evictor.evictBetween(from, fromInclusive, to, toInclusive);
            }
        }
    }
}
