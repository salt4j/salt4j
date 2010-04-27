package salt4j.cache;

/** Cache a single value. The main benefit is the proper synchronization. */
public class UnitCache<V> {
    V value = null;
    public synchronized V get() { return value; }
    public synchronized void put(V value) { this.value = value; }
    public synchronized void evict() { value = null; }
}
