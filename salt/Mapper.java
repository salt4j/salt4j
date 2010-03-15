package salt;

import java.util.Iterator;

/**
 * A class for transforming an Iterable/Iterator into another Iterable/Iterator.
 * It can also be combined with lastValue() to reduce an iterable to a value
 * @author Seun Osewa
 */
abstract public class Mapper<X, Y> {
    /**
     * Called for each element in the Inputstream.  A return value of null skips the current element.
     */
    abstract public  Y map(X element);

    /** Apply this transformation to each element of an iterable. */
    final public Stream<Y> applyTo(final Iterator<X> iterator) {
        return new Stream<Y>() {
            public Y produce() {
                while(iterator.hasNext()) {
                    final Y value = map(iterator.next());
                    if (value != null) return value;
                }
                return null;
            }
        };
    }

    final public Sequence<Y> applyTo(final Iterable<X> iterable) {
        return new Sequence<Y>() {
            public Stream<Y> iterator() { return applyTo(iterable.iterator()); }
        };
    }
}