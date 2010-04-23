package salt4j;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;

import java.util.ArrayList;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class Cache {
    final static public Cache GLOBAL = new Cache();

    final static class SoftRef extends SoftReference<Object> {
        final String key;
        SoftRef(String key, Object value, ReferenceQueue<Object> refq) {
            super(value, refq); this.key = key;
        }
    }

    final TreeMap<String, SoftRef> map = new TreeMap<String, SoftRef>();
    final ReferenceQueue<Object> refq = new ReferenceQueue<Object>();
    
    int hits = 0; int misses = 0; 
    int insertions = 0; int replacements = 0;
    int evictions = 0; int removals = 0;
    
    public void printStats() {
        System.out.println("hits: " + hits);
        System.out.println("misses: " + misses);
        System.out.println("insertions: " + insertions);
        System.out.println("replacements: " + replacements);
        System.out.println("evictions: " + evictions);
        System.out.println("removals: " + removals);
    }

    private final void gc() {
        SoftRef ref;
        while ((ref = (SoftRef)refq.poll()) != null) { map.remove(ref.key); evictions ++; }
    }

    public synchronized <E> void put(String key, E value) {
        gc();
        SoftRef ref = map.put(key, new SoftRef(key, value, refq));
        if (ref == null) insertions++; else replacements++;
    }

    public synchronized <E> E get(String key) {
        gc();
        SoftReference<Object> soft = map.get(key);
        if (soft == null) { misses++; return null; } //not in dict
        else {
            E b = (E)soft.get();
            if (b == null) { map.remove(key); misses++; return null; } // gc() => unreachable.
            else { hits++; return b; }
        }
    }

    public synchronized Cache clear() { 
        gc(); removals += map.size(); map.clear(); return this; }

    private final String getUpperBound(String s) {
        int last = s.length() - 1;
        char lastChar = (char)(s.charAt(last)+1);
        if (lastChar == 0) lastChar = (char)(0xFFFF);
        return s.substring(0, last) + lastChar;
    }

    private final SortedMap<String, SoftRef> getPrefix(String prefix) {
         return map.subMap(prefix, getUpperBound(prefix));
    }

    public final static class CacheEntry<E> {
        final String key; final E value;
        public CacheEntry(String key, E value) { this.key = key; this.value = value; }
    }

    public synchronized<E> ArrayList<CacheEntry<E>> getPrefixEntries(String prefix) {
        gc();
        SortedMap<String, SoftRef> subMap = getPrefix(prefix);
        ArrayList<CacheEntry<E>> list = new ArrayList<CacheEntry<E>>(subMap.size());
        for (Map.Entry<String, SoftRef> entry: subMap.entrySet()){
            E value = (E)entry.getValue().get();
            if (value == null) misses++;
            else { list.add(new CacheEntry<E>(entry.getKey(), value)); hits++; }
        }
        return list;
    }

    public synchronized Cache clearKeys(String prefix) {
        gc();
        SortedMap<String, SoftRef> subMap = getPrefix(prefix);
        removals += subMap.size();
        subMap.clear(); return this;
    }

    public synchronized Cache remove(String key) {
        gc();
        if (map.remove(key) != null) removals++;
        return this;
    }

    public synchronized int size() { gc(); return map.size(); }
}