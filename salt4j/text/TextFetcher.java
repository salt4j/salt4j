package salt4j.text;

import java.io.IOException;
import java.sql.SQLException;
import salt4j.cache.Cache;
import salt4j.core.Factory;

/** Helper class for fetching text fragments from a Cache with keys of type K */
abstract public class TextFetcher<E extends HtmlWriter, K> {
    public static transient boolean disableAll = false;

    abstract protected E generate(E dest) throws IOException, SQLException;

    final Cache<K, byte[]> cache; final E dest; final Factory<E> factory;
    public TextFetcher(Cache<K, byte[]> cache, Factory<E> factory, E dest) {
        this.cache = cache; this.factory = factory; this.dest = dest;
    }

    public final E fetch(K key) throws SQLException, IOException {
        if (disableAll) return generate(dest);
        else {
            byte[] htmlBytes;
            synchronized (cache) {
                htmlBytes = cache.get(key);
                if (htmlBytes == null) {
                    htmlBytes = generate(factory.create()).getBytes();
                    cache.put(key, htmlBytes);
                    setupInvalidators();
                }
            }
            dest.writeBytes(htmlBytes); return dest;
        }
    }

    /** Override to register the cache with an entry invalidator. */
    public void setupInvalidators() throws SQLException, IOException {}
}