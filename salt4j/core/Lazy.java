package salt4j.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import salt4j.Db;

/**
 *  Compute a value lazily and return it when demanded.  Caches the value after the first get()
 */
abstract public class Lazy<E, F extends Exception> {
    private E value = null;
    public abstract E compute() throws F;
    public boolean computed() { return value != null; }

    /** Compute the value of this computation or return the pre-computed value */
    public E get() throws F {
        if (value != null) return value; else return (value = compute());
    }

    public E getIfSet() { return value; }

    abstract public static class DB extends Lazy<salt4j.Db, SQLException>{
        public static DB wrap(final Lazy<salt4j.Db, SQLException> target) {
            return new DB() {
                public Db compute() throws SQLException { return target.compute(); }
            };
        }
    }
    abstract public static class Result extends Lazy<ResultSet, SQLException>{
        public static Result wrap(final Lazy<ResultSet, SQLException> target) {
            return new Result() {
                public ResultSet compute() throws SQLException { return target.compute(); }
            };
        }
    }
}
