package salt4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.sql.SQLException;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringEscapeUtils;

/**
 * Write buffer some unicode text and write it to the web.  Provides simple formatting.
 */
public class HtmlWriter {
    public static Charset UTF8 = Charset.forName("UTF-8");

    final ByteArrayOutputStream bytes = new ByteArrayOutputStream(1024);
    final Writer writer = new OutputStreamWriter(bytes, UTF8);

    private void replaceInto(String pattern, Object[] args) throws IOException {
        final int l = pattern.length();
        int formerPosition = 0;
        for (Object o: args) {
            int newPosition = pattern.indexOf("{?}", formerPosition);
            if (newPosition == -1) throw new RuntimeException("too many params");
            writer.append(pattern, formerPosition, newPosition);
            writer.append(o != null ? o.toString() : "{?}");
            formerPosition = newPosition + 3; // length of "{?}"
        }
        if (formerPosition < l && pattern.indexOf("{?}", formerPosition)!=-1) throw
                new RuntimeException("too few params");
        else writer.append(pattern, formerPosition, l);
    }

    public HtmlWriter echo(String string) throws IOException { writer.append(string); return this; }
    public HtmlWriter echo(Object o) throws IOException { writer.append(o.toString()); return this; }

    public HtmlWriter format(String pattern, Object ... args) throws IOException {
        for (int i = 0; i< args.length; i++) {
            if (args[i] instanceof String)
                args[i] = StringEscapeUtils.escapeHtml((String)args[i]);
        }
        replaceInto(pattern, args); return this;
    }

    public HtmlWriter writeBytes(byte[] b) throws IOException {
        writer.flush(); bytes.write(b); return this;
    }

    public HtmlWriter writeBytes(byte[] b, int offset, int len) throws IOException {
        writer.flush(); bytes.write(b, offset, len); return this;
    }

    public HtmlWriter flush() throws IOException { writer.flush(); return this; }

    public String getString() throws IOException { writer.flush(); return bytes.toString("UTF-8"); }

    public byte[] getBytes() throws IOException { writer.flush(); return bytes.toByteArray(); }
    
    public void writeTo(HttpServletResponse response) throws IOException {
        flush();
        if (response.getContentType() == null) response.setContentType("text/html;charset=UTF-8");
        response.setContentLength(bytes.size());
        bytes.writeTo(response.getOutputStream());
    }
    
    //FRAGMENT CACHING:
    protected static transient boolean NO_CACHE = false;
    protected final static Cache cache = Cache.GLOBAL;

    /**
     * A class to make caching of html fragments easy.
     * Subclass newWriter() globally and subclass generate() locally.
     */
    abstract public class AbstractCacheWrapper<E extends HtmlWriter> {
        abstract protected E generate(E writer) throws SQLException, IOException;
        abstract protected E newWriter();

        final String key; final E dest;
        public AbstractCacheWrapper(String key, E dest) {this.key = key; this.dest=dest;}

        public E get() throws SQLException, IOException {
            try {
                if (NO_CACHE) return generate(dest);
                else {
                    //by storing UTF8 bytes directly we can save ram and skip the conversion step.
                    byte[] htmlBytes;
                    synchronized (cache) {
                        htmlBytes = cache.get(key);
                        if (htmlBytes == null) {
                            htmlBytes = generate(newWriter()).getBytes();
                            cache.put(key, htmlBytes);
                        }
                    }
                    dest.writeBytes(htmlBytes); return dest;
                }
            } catch (SQLException e) { throw e; }
            catch (IOException e) { throw e; }
        }
    }
}