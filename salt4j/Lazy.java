package salt4j;

/** Compute a value lazily and return it when demanded.  Caches the value after the first get() */
abstract public class Lazy<E> {
    abstract E compute() throws Exception;
    private E value = null;
    /** Compute the value of this computation or return the pre-computed value */
    public E get() {
        try { if (value != null) return value; else return (value = compute()); }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    public boolean computed() { return value != null; }
}
