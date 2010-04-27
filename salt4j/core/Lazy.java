package salt4j.core;

import java.sql.SQLException;

/**
 *  Compute a value lazily and return it when demanded.  Caches the value after the first get()
*   the target use cases are Lazy<Db> and Lazy<ResultSet>, so these functions throw SQLException.
 */

abstract public class Lazy<E> {
    /** @throws SQLException because this is probably a database computation. */
    public abstract E compute() throws SQLException;
    private E value = null;
    /** Compute the value of this computation or return the pre-computed value */
    public E get() throws SQLException {
        if (value != null) return value; else return (value = compute());
    }
    public boolean computed() { return value != null; }
}
