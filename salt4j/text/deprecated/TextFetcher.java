package salt4j.text.deprecated;

import java.io.IOException;
import java.sql.SQLException;
import salt4j.cache.TmCache;
import salt4j.core.Factory;
import salt4j.text.HtmlWriter;

/** Helper class for fetching text fragments from a Cache with keys of type K */
abstract public class TextFetcher<E extends HtmlWriter, K> {
    public static transient boolean disableAll = false;

    abstract protected E generate(E dest) throws IOException, SQLException;

    final TmCache<K, byte[]> cache; final E dest; final Factory<E> factory;
    public TextFetcher(TmCache<K, byte[]> cache, Factory<E> factory, E dest) {
        this.cache = cache; this.factory = factory; this.dest = dest;
    }

    public final E fetch(K key) throws SQLException, IOException {
        if (disableAll) return generate(dest);
        else {
            byte[] htmlBytes = cache.get(key); //read lock.
            if (htmlBytes == null) {
                cache.writeLock(key);
                htmlBytes = generate(factory.create()).getBytes();
                cache.put(key, htmlBytes); //will upgrade read lock to write lock.
                setupInvalidators();
            }
            dest.writeBytes(htmlBytes); return dest;
        }
    }

    /** Override to register the cache with an entry invalidator. */
    public void setupInvalidators() throws SQLException, IOException {}
}