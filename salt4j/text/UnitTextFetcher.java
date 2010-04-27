package salt4j.text;

import java.io.IOException;
import java.sql.SQLException;
import salt4j.text.HtmlWriter;
import salt4j.cache.UnitCache;
import salt4j.core.Factory;

abstract public class UnitTextFetcher<E extends HtmlWriter> {
    public static transient boolean disableAll = false;

    abstract protected E generate(E dest) throws IOException, SQLException;

    final UnitCache<byte[]> cache; final E dest; final Factory<E> factory;
    public UnitTextFetcher(UnitCache<byte[]> cache, Factory<E> factory, E dest) {
        this.cache = cache; this.factory = factory; this.dest = dest;
    }

    public final E fetch() throws SQLException, IOException {
        if (disableAll) return generate(dest);
        else {
            byte[] htmlBytes;
            synchronized (cache) {
                htmlBytes = cache.get();
                if (htmlBytes == null) {
                    htmlBytes = generate(factory.create()).getBytes();
                    cache.put(htmlBytes);
                }
            }
            dest.writeBytes(htmlBytes); return dest;
        }
    }
}
