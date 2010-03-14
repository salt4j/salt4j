package salt4j;

/**
 * @author Seun Osewa
 */
public class Tuple {
    private final Object[] contents;
    public Tuple(Object... contents) { this.contents = contents; }
    public String toString() { return Sequence.wrap(contents).toString(); }
    public <E> E get(int index) { return (E)contents[index]; }
    public int getInt(int index) { return (Integer)contents[index]; }
    public String getString(int index) { return (String)contents[index]; }
}
