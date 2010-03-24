package salt4j.io;

import java.io.Closeable;
import java.io.IOException;

abstract public class Manager<X extends Closeable> {
    abstract public void run(X... closeables) throws IOException;

    public void handleIOError(IOException e) { e.printStackTrace(); }

    public void with(X... closeables) throws IOException {
        try { run(closeables); }
        catch(IOException e) { handleIOError(e); }
        finally { for (X x: closeables) try {x.close();}catch(IOException e){ }; }
    }

    abstract public static class One<X extends Closeable> extends Manager<X>{
        final public void run(X... closeables) throws IOException { run(closeables[0]); }
        abstract public void run(X closeable) throws IOException;

        public void with(X... closeables) throws IOException {
            throw new UnsupportedOperationException();
        }

        public void with(X closeable) throws IOException {
            try{ run(closeable); }
            catch(IOException e) { handleIOError(e); }
            try {closeable.close();}catch(IOException e){ };
        }
    }
}
