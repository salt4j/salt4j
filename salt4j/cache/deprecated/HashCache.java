package salt4j.cache.deprecated;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import salt4j.cache.Cache;

public class HashCache<K, V> implements Cache<K, V> {
    private static class CacheRef<K,V> extends SoftReference<V> {
        public final K key;
        public CacheRef(K key, V value, ReferenceQueue<V> refq) {
            super(value, refq); this.key = key;
        }
    }
    final HashMap<K, CacheRef<K,V>> map = new HashMap<K, CacheRef<K,V>>();
    final ReferenceQueue<V> refq = new ReferenceQueue<V>();

    int hits = 0; int misses = 0;
    int puts = 0; int evictions = 0;

    private final void gc() {
        CacheRef<K,V> ref;
        while ((ref = (CacheRef<K,V>)refq.poll()) != null) {
            map.remove(ref.key); evictions ++;
        }
    }

    public synchronized void put(K key, V value) {
        gc();
        map.put(key, new CacheRef<K, V>(key, value, refq));
        puts++;
    }

    public synchronized V get(K key) {
        gc();
        CacheRef<K,V> soft = map.get(key);
        if (soft == null) { misses++; return null; } //not in dict
        else {
            V b = soft.get();
            if (b == null) { map.remove(key); misses++; System.err.println("double-evict"); return null; }
            else { hits++; return b; }
        }
    }

    public synchronized void evict(K key) { gc(); map.remove(key); }
    
    public synchronized int size() { gc(); return map.size(); }
}