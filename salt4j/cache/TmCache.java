package salt4j.cache;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import salt4j.cache.Cache;
import salt4j.tm.TmLock;

public class TmCache<K, V> implements Cache<K, V> {
    private final static class CacheRef<K,V> extends SoftReference<V> {
        public final K key;
        public CacheRef(K key, V value, ReferenceQueue<V> refq) {
            super(value, refq); this.key = key;
        }
    }

    private final HashMap<K, CacheRef<K,V>>[] maps;
    private final ReferenceQueue<V>[] queues;
    private final TmLock locks[];
    private final int concurrency;
    
    public TmCache(int concurrency) {
        maps = (HashMap<K, CacheRef<K, V>>[])new HashMap[concurrency];
        queues = (ReferenceQueue<V>[])new ReferenceQueue[concurrency];
        locks = new TmLock[concurrency];
        for (int i = 0; i<concurrency; i++) {
            maps[i] = new HashMap<K, CacheRef<K, V>>();
            queues[i] = new ReferenceQueue<V>();
            locks[i] = new TmLock();
        }
        this.concurrency = concurrency;
    }

    private int getIndex(K key) { return (key.hashCode() & 0x8FFFFFF) % concurrency; } //lost 1 bit?

    private final void gc(ReferenceQueue refq, HashMap map) {
        CacheRef<K,V> ref;
        while ((ref = (CacheRef<K,V>)refq.poll()) != null) map.remove(ref.key);
    }

    public void gc() {
        for (int i = 0; i < concurrency; i++) locks[i].write();
        for (int i = 0; i < concurrency; i++) gc(queues[i], maps[i]);
    }

    public V get(K key) {
        final int i = getIndex(key);
        locks[i].read();
        CacheRef<K,V> soft = maps[i].get(key);
        return (soft==null) ? null : soft.get();
    }
    
    public void put(final K key, V value) {
        final int i = getIndex(key);
        locks[i].write();
        gc(queues[i], maps[i]);
        final CacheRef<K,V> formerRef = maps[i].put(key, new CacheRef<K, V>(key, value, queues[i]));
        if (formerRef != null) TmLock.addToUndoLog(new Runnable() {
            public void run() { maps[i].put(key, formerRef); }
        });
    }

    public void evict(final K key) {
        final int i = getIndex(key);
        locks[i].write();
        gc(queues[i], maps[i]);
        final CacheRef<K,V> formerRef = maps[i].remove(key);
        if (formerRef != null) TmLock.addToUndoLog(new Runnable() {
            public void run() { maps[i].put(key, formerRef); }
        });
    }
    
    public void writeLock(K key) { locks[getIndex(key)].write(); }

    public static void commit() { TmLock.commit(); }
    public static void rollback() { TmLock.rollback(); }
}